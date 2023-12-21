package com.creativeyann17.app.filters;

import com.creativeyann17.app.configurations.SecurityConfiguration;
import com.creativeyann17.app.utils.Const;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@Order(0)
@Slf4j
public class RootFilter extends OncePerRequestFilter {

  @Autowired
  @Qualifier("handlerExceptionResolver")
  private HandlerExceptionResolver resolver;

  @Autowired
  private SecurityConfiguration securityConfiguration;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
    try {
      request.setAttribute(Const.RequestAttr.x_req_start.name(), System.currentTimeMillis());
      filterChain.doFilter(request, response);
    } catch (Exception e) {
      resolver.resolveException(request, response, null, e);
    } finally {
      logRequest(request, response);
    }
  }

  private void logRequest(HttpServletRequest request, HttpServletResponse response) {
    if (securityConfiguration.getPublics().stream().noneMatch(p -> request.getRequestURI().equals(p))) {
      long start = (long) request.getAttribute(Const.RequestAttr.x_req_start.name());
      log.info("{} {} {} {} in {} ms", request.getRemoteAddr(), request.getMethod(), response.getStatus(), request.getRequestURI(), System.currentTimeMillis() - start);
    }
  }

}
