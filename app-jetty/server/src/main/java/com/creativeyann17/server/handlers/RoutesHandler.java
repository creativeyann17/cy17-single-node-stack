package com.creativeyann17.server.handlers;

import com.creativeyann17.server.context.Context;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class RoutesHandler {

  private final Route rootRoute = new Route(null, "", null, null);
  private AccessHandler accessHandler = new AccessHandlerImpl();

  public void handle(Context ctx) {
    if (!handle(ctx, rootRoute.routes)) {
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
            ctx.request().setRoute(route);
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
    rootRoute.add(method, path, handler, roles);
    return this;
  }

  public Route subRoute(String path, RouteRole... roles) {
    return rootRoute.subRoute(path, roles);
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
    private Map<String, Integer> params;
    private final String fullPath;

    public Route(HttpMethod method, String path, Consumer<Context> handler, Route parent, RouteRole... roles) {
      this.method = method;
      this.path = parsePathParam(path);
      this.handler = handler;
      this.parent = parent;
      this.roles = parent != null && roles.length == 0 ? parent.roles : roles;
      if (isSubRoute()) {
        this.routes = new ArrayList<>();
      }
      this.fullPath = computeFullPath();
    }

    public Route add(HttpMethod method, String path, Consumer<Context> handler, RouteRole... roles) {
      assert method != null && path != null && handler != null;
      routes.add(new Route(method, path, handler, this, roles));
      return this;
    }

    public Route subRoute(String path, RouteRole... roles) {
      assert path != null;
      var subRoute = new Route(null, path, null, this, roles);
      routes.add(subRoute);
      return subRoute;
    }

    public String fullPath() {
     return this.fullPath;
    }

    private String computeFullPath() {
      String fullPath = "";
      if (parent != null) {
        fullPath += parent.fullPath();
      }
      return (fullPath + this.path).replaceFirst("//","/");
    }

    public boolean isSubRoute() {
      return this.method == null && this.handler == null;
    }

    public boolean matches(Context ctx) {
      return List.of(HttpMethod.HEAD, method).contains(ctx.request().method()) && ctx.request().path().matches(fullPath);
    }

    public boolean hasRole(List<? extends RouteRole> userRoles) {
      return roles == null || roles.length == 0 || Stream.of(roles).anyMatch(userRoles::contains);
    }

    public Map<String, Integer> getParams() {
      return params;
    }

    private String parsePathParam(String path) {
      String pathWithParam = "/";
      var parts = sanitizePath(path).split("/");
      for (int i = 0; i < parts.length; i++) {
        var part = parts[i];
        if (StringUtil.isNotBlank(part)) {
          if (part.startsWith(":")) {
            if (params == null)
              params = new HashMap<>();
            params.put(part.replace(":", ""), i);
            pathWithParam += "/\\w+";
          } else {
            pathWithParam += "/" + part;
          }
        }
      }
      return pathWithParam.replaceFirst("//","/");
    }

    private String sanitizePath(String path) {
      if (!path.startsWith("/"))
        path = "/" + path;
      while (path.contains("//"))
        path = path.replaceAll("//", "/");
      path = path.replace("/*", "/\\w+");
      return path;
    }
  }
}
