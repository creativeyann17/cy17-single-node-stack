package com.creativeyann17.app;

import io.javalin.http.Context;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class RequestLogger {

  public void before(Context ctx) {
    ctx.attribute("x_req_start", System.currentTimeMillis());
  }

  public void after(Context ctx) {
    if (App.PUBLICS.stream().noneMatch(p -> ctx.req().getRequestURI().equals(p))) {
      long start = (long) ctx.req().getAttribute("x_req_start");
      log.info("{} {} {} {} in {} ms", getRemoteAddr(ctx.req()), ctx.req().getMethod(), ctx.res().getStatus(), ctx.req().getRequestURI(), System.currentTimeMillis() - start);
    }
  }

  private String getRemoteAddr(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader("X-Real-IP")).orElse(request.getRemoteAddr());
  }
}
