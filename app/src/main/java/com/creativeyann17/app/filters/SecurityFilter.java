package com.creativeyann17.app.filters;

import com.creativeyann17.app.configurations.SecurityConfiguration;
import com.creativeyann17.app.services.LogOnceService;
import com.creativeyann17.app.utils.Const;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class SecurityFilter extends OncePerRequestFilter {

  private final SecurityConfiguration securityConfiguration;
  private final HttpServletRequest currentRequest;
  private final LogOnceService logOnceService;

  @Override
  public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    if (securityConfiguration.isEnabled()) {
      checkIsSystemUser(request);
    }
    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    return HttpMethod.OPTIONS.name().equals(request.getMethod()) ||
      this.securityConfiguration.getPublics().stream().anyMatch(p -> request.getRequestURI().equals(p)) ||
      super.shouldNotFilter(request);
  }

  private void checkIsSystemUser(HttpServletRequest request) {
    boolean isSystemUser = false;
    var apiKey = securityConfiguration.getApiKey();
    if (StringUtils.isNotBlank(apiKey)) {
      var userApiKey = request.getHeader(Const.RequestHeader.X_API_KEY.value);
      if (userApiKey != null) {
        if (apiKey.equals(userApiKey)) {
          isSystemUser = true;
        } else {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
      }
    } else {
      logOnceService.warn("Missing secured API key in configuration");
    }
    request.setAttribute(Const.RequestAttr.x_req_is_system.name(), isSystemUser);
  }

  public boolean isSystem() {
    return Boolean.TRUE.equals(currentRequest.getAttribute(Const.RequestAttr.x_req_is_system.name()));
  }
}
