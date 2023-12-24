package com.creativeyann17.server.context;

import org.eclipse.jetty.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

public class Request {

  private final org.eclipse.jetty.server.Request request;
  private Map<String, String> queryParams;

  public Request(org.eclipse.jetty.server.Request request) {
    this.request = request;
  }

  private void extractQueryParams() {
    if (queryParams == null)
      queryParams = new HashMap<>();
    var query = request.getHttpURI().getQuery();
    if (query != null) {
      for (String tok : query.split("&")) {
        var pair = tok.split("=");
        queryParams.put(pair[0], pair[1]);
      }
    }
  }

  public HttpMethod method() {
    return HttpMethod.valueOf(request.getMethod());
  }

  public String path() {
    return request.getHttpURI().getPath();
  }

  public Object attribute(String key) {
    return request.getAttribute(key);
  }

  public void attribute(String key, Object value) {
    request.setAttribute(key, value);
  }

  public String header(String name) {
    return request.getHeaders().get(name);
  }

  public String queryParam(String key) {
    this.extractQueryParams();
    return queryParams.get(key);
  }

  public String getRemoteAddr() {
    return request.getConnectionMetaData().getRemoteSocketAddress().toString();
  }

  public String getHeader(String name) {
    return request.getHeaders().get(name);
  }
}
