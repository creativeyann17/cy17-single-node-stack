package com.creativeyann17.server.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Callback;

public class Context {

  private final Request request;
  private final Response response;
  private final Callback callback;
  private final ObjectMapper objectMapper;

  public Context(org.eclipse.jetty.server.Request request, org.eclipse.jetty.server.Response response, Callback callback, ObjectMapper objectMapper) {
    this.request = new Request(request);
    this.response = new Response(request, response);
    this.callback = callback;
    this.objectMapper = objectMapper;
  }

  public void empty(int code) {
    this.text(code, "");
  }

  public void json(Object object) {
    json(HttpStatus.OK_200, object);
  }

  public void json(int code, Object object) {
    try {
      this.response.write(code, "application/json", objectMapper.writeValueAsString(object), callback);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void text(String message) {
    this.text(HttpStatus.OK_200, message);
  }

  public void text(int code, String message) {
    this.response.write(code, "text/plain", message, callback);
  }

  public Request request() {
    return this.request;
  }

  public void header(HttpHeader header, Object value) {
    response.header(header, value);
  }

  public void header(String name, Object value) {
    response.header(name, value);
  }
  
  public Response response() {
    return this.response;
  }

}
