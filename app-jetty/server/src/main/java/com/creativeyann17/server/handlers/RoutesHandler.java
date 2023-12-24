package com.creativeyann17.server.handlers;

import com.creativeyann17.server.context.Context;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class RoutesHandler {

  private final List<Route> routes = new ArrayList<>();
  private AccessHandler accessHandler = new AccessHandlerImpl();

  public void handle(Context ctx) {
    if (!handle(ctx, routes)) {
      ctx.empty(HttpStatus.NOT_FOUND_404);
    }
  }

  private boolean handle(Context ctx, List<Route> routes) {
    for (Route route : routes) {
      if (route.isSubRoute()) {
        if (this.handle(ctx, route.routes)) {
          return true;
        }
      } else {
        if (route.matches(ctx)) {
          var roles = this.accessHandler.apply(ctx);
          if (route.hasRole(roles)) {
            route.handler.accept(ctx);
          } else {
            ctx.empty(HttpStatus.FORBIDDEN_403);
          }
          return true;
        }
      }
    }
    return false;
  }

  public RoutesHandler add(HttpMethod method, String path, Consumer<Context> handler, RouteRole... roles) {
    assert method != null && path != null && handler != null;
    routes.add(new Route(method, path, handler, null, roles));
    return this;
  }

  public Route subRoute(String path, RouteRole... roles) {
    assert path != null;
    var subRoute = new Route(null, path, null, null, roles);
    routes.add(subRoute);
    return subRoute;
  }

  public void setAccessHandler(AccessHandler accessHandler) {
    this.accessHandler = accessHandler;
  }

  public static class Route {

    private final HttpMethod method;
    private final String path;
    private final Consumer<Context> handler;
    private final Route parent;
    private final RouteRole[] roles;
    private List<Route> routes;

    public Route(HttpMethod method, String path, Consumer<Context> handler, Route parent, RouteRole... roles) {
      this.method = method;
      this.path = path;
      this.handler = handler;
      this.parent = parent;
      this.roles = parent != null && roles.length == 0 ? parent.roles : roles;
    }

    public Route add(HttpMethod method, String path, Consumer<Context> handler, RouteRole... roles) {
      assert method != null && path != null && handler != null;
      if (routes == null)
        routes = new ArrayList<>();
      routes.add(new Route(method, path, handler, this, roles));
      return this;
    }

    public String fullPath() {
      String fullPath = "";
      if (parent != null) {
        fullPath += parent.path;
      }
      return fullPath + this.path;
    }

    public boolean isSubRoute() {
      return this.method == null && this.handler == null;
    }

    public boolean matches(Context ctx) {
      return (List.of(HttpMethod.HEAD, method).contains(ctx.request().method())) && ctx.request().path().equals(fullPath());
    }

    public boolean hasRole(List<? extends RouteRole> userRoles) {
      return roles == null || roles.length == 0 || Stream.of(roles).anyMatch(userRoles::contains);
    }
  }
}
