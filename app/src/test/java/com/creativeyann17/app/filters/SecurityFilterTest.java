package com.creativeyann17.app.filters;

import com.creativeyann17.app.configurations.SecurityConfiguration;
import com.creativeyann17.app.services.LogOnceService;
import com.creativeyann17.app.utils.Const;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityFilterTest {

  SecurityConfiguration configuration = Mockito.mock(SecurityConfiguration.class);
  HttpServletRequest currentRequest = Mockito.mock(HttpServletRequest.class);
  LogOnceService logOnceService = Mockito.mock(LogOnceService.class);
  FilterChain filterChain = Mockito.mock(FilterChain.class);
  SecurityFilter filter = new SecurityFilter(configuration, currentRequest, logOnceService);

  @BeforeEach
  void beforeEach() {
    when(currentRequest.getHeader(Const.RequestHeader.X_API_KEY.name())).thenReturn(null);
    when(configuration.isEnabled()).thenReturn(true);
    when(configuration.getApiKey()).thenReturn("foo");
  }

  @Test
  void apiKeyNotConfigured() throws ServletException, IOException {
    when(configuration.getApiKey()).thenReturn("");
    var request = Mockito.mock(HttpServletRequest.class);
    filter.doFilterInternal(request, null, filterChain);
    verify(logOnceService).warn("Missing secured API key in configuration");
  }

  @Test
  void unauthorized() {
    when(currentRequest.getHeader(Const.RequestHeader.X_API_KEY.name())).thenReturn("bar");
    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      filter.doFilterInternal(currentRequest, null, filterChain);
    });
    assertNull(exception.getReason());
    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
  }

  @Test
  void isSystem() throws ServletException, IOException {
    when(currentRequest.getHeader(Const.RequestHeader.X_API_KEY.name())).thenReturn("foo");
    filter.doFilterInternal(currentRequest, null, filterChain);
    verify(currentRequest).setAttribute(Const.RequestAttr.x_req_is_system.name(), true);
    when(currentRequest.getAttribute(Const.RequestAttr.x_req_is_system.name())).thenReturn(true);
    assertTrue(filter.isSystem());
  }

  @Test
  void isNotSystem() throws ServletException, IOException {
    filter.doFilterInternal(currentRequest, null, filterChain);
    verify(currentRequest).setAttribute(Const.RequestAttr.x_req_is_system.name(), false);
    when(currentRequest.getAttribute(Const.RequestAttr.x_req_is_system.name())).thenReturn(false);
    assertFalse(filter.isSystem());
    when(currentRequest.getAttribute(Const.RequestAttr.x_req_is_system.name())).thenReturn(null);
    assertFalse(filter.isSystem());
  }

}
