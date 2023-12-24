package com.creativeyann17.server.context;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.StringUtil;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Response {

  private final org.eclipse.jetty.server.Response response;

  public Response(org.eclipse.jetty.server.Response response) {
    this.response = response;
  }

  public void write(int code, String contentType, String message, Callback callback) {
    response.setStatus(code);
    response.getHeaders().add(HttpHeader.CONTENT_TYPE, contentType);
    if (StringUtil.isNotBlank(message)) {
      response.write(true, UTF_8.encode(message), callback);
    }
  }

  public int status() {
    return response.getStatus();
  }
}
