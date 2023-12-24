package com.creativeyann17.server.handlers;

import com.creativeyann17.server.context.Context;
import org.eclipse.jetty.http.HttpHeader;

import java.util.Optional;

public class CorsHandler {

  public void handle(Context ctx) {
    var origin = ctx.request().header(HttpHeader.ORIGIN);
    ctx.response().header("Access-Control-Allow-Origin", Optional.ofNullable(origin).orElse("*"));
    ctx.response().header("Access-Control-Allow-Methods", "*");
    ctx.response().header("Access-Control-Allow-Headers", "*");
    ctx.response().header("Access-Control-Expose-Headers", "*");
    ctx.response().header("Access-Control-Allow-Credentials", true);
    ctx.response().header("Access-Control-Max-Age", 3600);
  }
}
