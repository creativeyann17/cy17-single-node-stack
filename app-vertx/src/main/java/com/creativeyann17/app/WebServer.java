package com.creativeyann17.app;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.impl.FaviconHandlerImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static com.creativeyann17.app.HelloConsumer.HELLO_EVENT;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

@Slf4j
public class WebServer extends AbstractVerticle {

  private static final String APPLICATION_JSON = HttpHeaderValues.APPLICATION_JSON.toString();

  @Override
  public void start(Promise<Void> startPromise) {
    vertx
      .createHttpServer(createOptions())
      .requestHandler(createRouter())
      .listen(result -> {
        if (result.succeeded()) {
          startPromise.complete();
          log.info("HTTP server started on port: " + App.PORT);
        } else {
          startPromise.fail(result.cause());
        }
      });
  }

  private HttpServerOptions createOptions() {
    return new HttpServerOptions()
      .setCompressionSupported(false)
      .setPort(App.PORT);
  }

  private Router createRouter() {
    var router = Router.router(vertx);
    router.route().failureHandler(this::handleGlobalFailure);
    router.route().handler(this::handleBefore);
    router.route("/actuator/*").subRouter(createActuators());
    router.route("/api/v1/*").subRouter(createAPIv1());
    router.route().handler(this::handleNotFound);
    return router;
  }

  private void handleBefore(RoutingContext ctx) {
    ctx.put("x_req_start", System.currentTimeMillis());
    ctx.next();
  }

  private void handleAfter(RoutingContext ctx) {
    if (App.PUBLICS.stream().noneMatch(p -> ctx.request().uri().equals(p))) {
      long start = (long) ctx.get("x_req_start");
      log.info("{} {} {} {} in {} ms", getRemoteAddr(ctx), ctx.request().method(), ctx.response().getStatusCode(), ctx.request().absoluteURI(), System.currentTimeMillis() - start);
    }
    ctx.next();
  }

  private void handleHealth(RoutingContext ctx) {
    ctx.response().end(JsonObject.of("status", "UP").encode());
  }

  /*
    private void handleAuth(RoutingContext routingContext, boolean strict, Handler<String> handler) {
      var auth = routingContext.request().getHeader(AUTHORIZATION);
      if (StringUtils.isBlank(Configuration.X_API_KEY) || !Configuration.X_API_KEY.equals(StringUtils.replace(auth, "Bearer ", ""))) {
        if (strict) {
          routingContext.response().setStatusCode(403).end();
        } else {
          handler.handle(null);
        }
      } else {
        handler.handle(auth);
      }
    }
  */
  private Router createActuators() {
    var router = Router.router(vertx);
    router.route().produces(APPLICATION_JSON);
    router.route().handler(context -> {
      context.response().headers().add(CONTENT_TYPE, APPLICATION_JSON);
      context.next();
    });
    router.route("/health").handler(this::handleHealth);
    return router;
  }

  private Router createAPIv1() {
    var router = Router.router(vertx);
    router.route().consumes(APPLICATION_JSON);
    router.route().produces(APPLICATION_JSON);
    router.route().handler(BodyHandler.create());
    router.route().handler(context -> {
      context.response().headers().add(CONTENT_TYPE, APPLICATION_JSON);
      context.next();
    });
    router.route("/hello").handler(this::handleHello);
    return router;
  }

  private void handleGlobalFailure(RoutingContext routingContext) {
    var exception = routingContext.failure();
    log.error("", exception);
    routingContext.response().setStatusCode(500).end();
  }

  private void handleNotFound(RoutingContext rc) {
    rc.response().setStatusCode(404).end();
  }


  private void handleHello(RoutingContext routingContext) {
    vertx.eventBus().request(HELLO_EVENT, App.APP_NAME).onSuccess(handler -> {
      routingContext.response()
        .putHeader(CONTENT_TYPE, TEXT_PLAIN)
        .end(String.valueOf(handler.body()));
    }).onFailure(routingContext::fail);
  }

  private String getRemoteAddr(RoutingContext routingContext) {
    return Optional.ofNullable(routingContext.request().getHeader("X-Real-IP")).orElse(routingContext.request().remoteAddress().toString());
  }

}
