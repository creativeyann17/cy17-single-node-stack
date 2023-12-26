package com.creativeyann17.server.handlers;

import com.creativeyann17.server.context.Context;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoutesHandlerTest {

  HttpURI httpURI = Mockito.mock(HttpURI.class);
  Request request = Mockito.mock(Request.class);
  Response response = Mockito.mock(Response.class);
  Context ctx = new Context(request, response, null, null);
  RoutesHandler routesHandler = new RoutesHandler();

  @BeforeEach
  void beforeEach() {
    when(response.getHeaders()).thenReturn(Mockito.mock(HttpFields.Mutable.class));
    when(request.getHttpURI()).thenReturn(httpURI);
  }

  @Test
  void assert_add(){
    assertThrows(AssertionError.class, () -> {
      routesHandler.add(null, null, null);
    });
  }

  @Test
  void assert_subRoute(){
    assertThrows(AssertionError.class, () -> {
      routesHandler.subRoute(null);
    });
  }

  @Test
  void handle_route() {
    routesHandler.add(HttpMethod.GET, "/foo", ctx -> {
      ctx.text(HttpStatus.NO_CONTENT_204, "");
    });
    when(request.getMethod()).thenReturn(HttpMethod.GET.name());
    when(httpURI.getPath()).thenReturn("/foo");
    routesHandler.handle(ctx);
    verify(response).setStatus(HttpStatus.NO_CONTENT_204);
  }

  @Test
  void handle_HEAD_route() {
    routesHandler.add(HttpMethod.GET, "/foo", ctx -> {
      ctx.text(HttpStatus.NO_CONTENT_204, "bar");
    });
    when(request.getMethod()).thenReturn(HttpMethod.HEAD.name());
    when(httpURI.getPath()).thenReturn("/foo");
    routesHandler.handle(ctx);
    verify(response).setStatus(HttpStatus.NO_CONTENT_204);
  }

  @Test
  void handle_sub_route() {
    routesHandler.subRoute("/path")
        .add(HttpMethod.POST, "/foo", ctx -> {
          ctx.text(HttpStatus.NO_CONTENT_204, "");
        });
    when(request.getMethod()).thenReturn(HttpMethod.POST.name());
    when(httpURI.getPath()).thenReturn("/path/foo");
    routesHandler.handle(ctx);
    verify(response).setStatus(HttpStatus.NO_CONTENT_204);
  }

  @Test
  void handle_path_not_found() {
    when(request.getMethod()).thenReturn(HttpMethod.GET.name());
    when(httpURI.getPath()).thenReturn("/foo");
    routesHandler.handle(ctx);
    verify(response).setStatus(HttpStatus.NOT_FOUND_404);
  }

  @Test
  void handle_method_route_method() {
    routesHandler.add(HttpMethod.GET, "/foo", ctx -> {
      ctx.text(HttpStatus.NO_CONTENT_204, "");
    });
    when(request.getMethod()).thenReturn(HttpMethod.POST.name());
    when(httpURI.getPath()).thenReturn("/foo");
    routesHandler.handle(ctx);
    verify(response).setStatus(HttpStatus.NOT_FOUND_404);
  }

  @Test
  void handle_forbidden_route() {
    routesHandler.add(HttpMethod.GET, "/foo", ctx -> {
      ctx.text(HttpStatus.NO_CONTENT_204, "");
    }, Role.ROLE_1);
    when(request.getMethod()).thenReturn(HttpMethod.GET.name());
    when(httpURI.getPath()).thenReturn("/foo");
    routesHandler.handle(ctx);
    verify(response).setStatus(HttpStatus.FORBIDDEN_403);
  }

  @Test
  void handle_forbidden_sub_route() {
    routesHandler.subRoute("/path", Role.ROLE_1)
      .add(HttpMethod.POST, "/foo", ctx -> {
        ctx.text(HttpStatus.NO_CONTENT_204, "");
      });
    when(request.getMethod()).thenReturn(HttpMethod.POST.name());
    when(httpURI.getPath()).thenReturn("/path/foo");
    routesHandler.handle(ctx);
    verify(response).setStatus(HttpStatus.FORBIDDEN_403);
  }

  @Test
  void handle_override_roles_sub_route() {
    routesHandler.subRoute("/path", Role.ROLE_1)
      .add(HttpMethod.POST, "/foo", ctx -> {
        ctx.text(HttpStatus.NO_CONTENT_204, "");
      }, Role.ROLE_2);
    when(request.getMethod()).thenReturn(HttpMethod.POST.name());
    when(httpURI.getPath()).thenReturn("/path/foo");
    routesHandler.setAccessHandler((context -> List.of(Role.ROLE_2)));
    routesHandler.handle(ctx);
    verify(response).setStatus(HttpStatus.NO_CONTENT_204);
  }

  private enum Role implements RouteRole {
    ROLE_1, ROLE_2
  }

  @Test
  void handle_route_with_param() {
    routesHandler.add(HttpMethod.GET, "/param/:p1/foo/:p2/bar", ctx -> {
        ctx.text(HttpStatus.NO_CONTENT_204, "");
        assertEquals("1", ctx.request().pathParam("p1"));
        assertEquals("2", ctx.request().pathParam("p2"));
      });
    when(request.getMethod()).thenReturn(HttpMethod.GET.name());
    when(httpURI.getPath()).thenReturn("/param/1/foo/2/bar");
    routesHandler.handle(ctx);
    verify(response).setStatus(HttpStatus.NO_CONTENT_204);
  }

  @Test
  void handle_route_with_star() {
    routesHandler.add(HttpMethod.GET, "/foo/*", ctx -> {
      ctx.text(HttpStatus.NO_CONTENT_204, "");
    });
    when(request.getMethod()).thenReturn(HttpMethod.GET.name());
    when(httpURI.getPath()).thenReturn("/foo/bar");
    routesHandler.handle(ctx);
    verify(response).setStatus(HttpStatus.NO_CONTENT_204);
  }

  @Test
  void handle_route_root() {
    routesHandler.add(HttpMethod.GET, "/", ctx -> {
      ctx.text(HttpStatus.NO_CONTENT_204, "");
    });
    when(request.getMethod()).thenReturn(HttpMethod.GET.name());
    when(httpURI.getPath()).thenReturn("/");
    routesHandler.handle(ctx);
    verify(response).setStatus(HttpStatus.NO_CONTENT_204);
  }

  @Test
  void handle_route_with_query_param() {
    routesHandler.add(HttpMethod.GET, "/foo", ctx -> {
      ctx.text(HttpStatus.NO_CONTENT_204, "");
      assertEquals("100", ctx.request().queryParam("p1"));
      assertEquals("bar", ctx.request().queryParam("p2"));
    });
    when(request.getMethod()).thenReturn(HttpMethod.GET.name());
    when(httpURI.getPath()).thenReturn("/foo");
    when(httpURI.getQuery()).thenReturn("p1=100&p2=bar");
    routesHandler.handle(ctx);
    verify(response).setStatus(HttpStatus.NO_CONTENT_204);
  }

  @Test
  void check_route_path() {
    assertEquals("/", new RoutesHandler.Route(HttpMethod.GET, "/", null, null).fullPath());
    assertEquals("/foo/bar", new RoutesHandler.Route(HttpMethod.GET, "foo////bar//", null, null).fullPath());
    assertEquals("/foo/\\w+/bar/\\w+", new RoutesHandler.Route(HttpMethod.GET, "/foo/:param1/bar/*", null, null).fullPath());
    assertEquals(Map.of("param1",2, "param2", 4), new RoutesHandler.Route(HttpMethod.GET, "/foo/:param1/bar/:param2", null, null).getParams());
  }

}
