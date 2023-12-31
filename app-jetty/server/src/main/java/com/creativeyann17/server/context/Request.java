package com.creativeyann17.server.context;

import com.creativeyann17.server.handlers.RoutesHandler;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;

public class Request {

  private final org.eclipse.jetty.server.Request request;
  private Fields queryParams;
  private RoutesHandler.Route route;

  public Request(org.eclipse.jetty.server.Request request) {
    this.request = request;
  }

  public HttpMethod method() {
    return HttpMethod.valueOf(request.getMethod());
  }

  public String path() {
    return request.getHttpURI().getPath();
  }

  public <T> T attribute(String key, Class<T> c) {
    return c.cast(request.getAttribute(key));
  }

  public void attribute(String key, Object value) {
    request.setAttribute(key, value);
  }

  public String header(String name) {
    return request.getHeaders().get(name);
  }

  public String header(HttpHeader header) {
    return request.getHeaders().get(header);
  }

  public String queryParam(String name) {
    if (this.queryParams == null)
      this.queryParams = org.eclipse.jetty.server.Request.extractQueryParameters(request);
    return queryParams.getValue(name);
  }

  public String pathParam(String name) {
    return path().split("/")[route.getParams().get(name)];
  }

  public String getRemoteAddr() {
    return org.eclipse.jetty.server.Request.getRemoteAddr(request);
  }

  public void setRoute(RoutesHandler.Route route) {
    this.route = route;
  }

}
