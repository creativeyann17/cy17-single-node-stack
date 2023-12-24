package com.creativeyann17.server.handlers;

import com.creativeyann17.server.context.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.util.function.Consumer;

public class GlobalHandler extends Handler.Abstract {

  private final ObjectMapper objectMapper;
  private final ErrorHandler errorHandler = new ErrorHandler();
  private final RoutesHandler routesHandler = new RoutesHandler();

  private Consumer<Context> before;
  private Consumer<Context> after;

  public GlobalHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean handle(Request request, Response response, Callback callback) {
    var context = new Context(request, response, callback, objectMapper);
    try {
      if (this.before != null) {
        this.before.accept(context);
      }
      this.routesHandler.handle(context);
    } catch (Exception e) {
      // avoid jetty error handler
      this.errorHandler.handle(e, context);
    } finally {
      if (this.after != null) {
        this.after.accept(context);
      }
    }
    callback.succeeded();
    return true;
  }

  public void setBefore(Consumer<Context> handler) {
    this.before = handler;
  }

  public void setAfter(Consumer<Context> handler) {
    this.after = handler;
  }

  public RoutesHandler getRoutesHandler() {
    return this.routesHandler;
  }

  public void setAccessHandler(AccessHandler accessHandler) {
    this.routesHandler.setAccessHandler(accessHandler);
  }
}
