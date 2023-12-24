package com.creativeyann17.server.context;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.StringUtil;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Response {

  private final Request request;
  private final org.eclipse.jetty.server.Response response;

  public Response(Request request, org.eclipse.jetty.server.Response response) {
    this.request = request;
    this.response = response;
  }

  public void write(int code, String contentType, String message, Callback callback) {
    response.setStatus(code);
    response.getHeaders().add(HttpHeader.CONTENT_TYPE, contentType);
    if (!request.getMethod().equals(HttpMethod.HEAD.name()) && StringUtil.isNotBlank(message)) {
      response.write(true, UTF_8.encode(message), callback);
    }
  }

  public int status() {
    return response.getStatus();
  }

  public void header(HttpHeader header, Object value) {
    response.getHeaders().add(header, value.toString());
  }

  public void header(String name, Object value) {
    response.getHeaders().add(name, value.toString());
  }
}
