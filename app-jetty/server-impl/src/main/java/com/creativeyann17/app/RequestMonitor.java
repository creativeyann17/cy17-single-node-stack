package com.creativeyann17.app;

import com.creativeyann17.server.context.Context;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class RequestMonitor {

  public void before(Context ctx) {
    ctx.request().attribute("monitor", MonitorFactory.start(ctx.request().method() + " " + ctx.request().path()));
  }

  public void after(Context ctx) {
    ((Monitor) ctx.request().attribute("monitor")).stop();
  }
}
