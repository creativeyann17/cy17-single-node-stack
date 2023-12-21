package com.creativeyann17.app;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import io.javalin.http.Context;

public class RequestMonitor {

  public void before(Context ctx) {
    ctx.attribute("monitor", MonitorFactory.start(ctx.req().getMethod() + " " + ctx.req().getRequestURI()));
  }

  public void after(Context ctx) {
    ((Monitor) ctx.attribute("monitor")).stop();
  }
}
