package com.creativeyann17.app;

import com.creativeyann17.server.context.Context;
import com.creativeyann17.server.context.Request;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class RequestLogger {

  public void before(Context ctx) {
    ctx.request().attribute("x_req_start", System.currentTimeMillis());
  }

  public void after(Context ctx) {
    if (App.PUBLICS.stream().noneMatch(p -> ctx.request().path().equals(p))) {
      long start = ctx.request().attribute("x_req_start", Long.class);
      log.info("{} {} {} {} in {} ms", getRemoteAddr(ctx.request()), ctx.request().method(), ctx.response().status(), ctx.request().path(), System.currentTimeMillis() - start);
    }
  }

  private String getRemoteAddr(Request request) {
    return Optional.ofNullable(request.header("X-Real-IP")).orElse(request.getRemoteAddr());
  }
}
