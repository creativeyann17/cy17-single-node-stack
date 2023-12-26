package com.creativeyann17.server.context;

import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ContextTest {

  HttpURI httpURI = Mockito.mock(HttpURI.class);
  org.eclipse.jetty.server.Request request = Mockito.mock(Request.class);
  org.eclipse.jetty.server.Response response = Mockito.mock(Response.class);
  Context ctx = new Context(request, response, null, null);

  @BeforeEach
  void beforeEach() {
    when(request.getHttpURI()).thenReturn(httpURI);
  }

  @Test
  void queryParam() {
    when(httpURI.getQuery()).thenReturn("size=foo&count=bar");
    assertEquals("foo", ctx.request().queryParam("size"));
    assertEquals("bar", ctx.request().queryParam("count"));
  }
}
