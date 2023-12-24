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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

}
