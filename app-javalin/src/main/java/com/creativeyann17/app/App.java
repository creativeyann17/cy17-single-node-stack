package com.creativeyann17.app;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.HttpStatus;
import io.javalin.json.JavalinJackson;
import io.javalin.security.RouteRole;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Slf4j
public class App {

  public static final List<String> PUBLICS = List.of("/actuator/health", "/favicon.ico");
  public static final Integer PORT = Optional.ofNullable(System.getenv("PORT")).map(Integer::parseInt).orElse(8080);
  public static final String ENV = Optional.ofNullable(System.getenv("ENV")).orElse("prod");
  public static final String X_API_KEY = Optional.ofNullable(System.getenv("X_API_KEY")).orElseThrow(() -> new RuntimeException("Missing secured API key in configuration"));
  public static final String APP_NAME = Optional.ofNullable(System.getenv("APP_NAME")).orElse("");
  public static final long START = System.currentTimeMillis();

  private static final Status status = new Status(APP_NAME, START);
  private static final RequestLogger requestLogger = new RequestLogger();
  private static final RequestMonitor requestMonitor = new RequestMonitor();
  private static final Health health = new Health("UP");

  static {
    setRootLevel("prod".equals(ENV) ? Level.ERROR : Level.INFO);
    log.info("App is starting with Cores: {}", Runtime.getRuntime().availableProcessors());
    log.info("Current profile: {}", ENV);
  }

  public static void main(String[] args) {
    var app = Javalin.create(config -> {
        config.showJavalinBanner = false;
        config.compression.none();
        config.plugins.enableCors(cors -> {
          cors.add(it -> {
            it.anyHost();
            it.exposeHeader("*");
            it.maxAge = 3600;
            it.allowCredentials = true;
          });
        });
        config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
          mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
          mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        }));
        config.accessManager((handler, ctx, routeRoles) -> {
          Role role = getUserRole(ctx);
          if (PUBLICS.stream().anyMatch(p -> ctx.req().getRequestURI().equals(p)) || routeRoles.contains(Role.ANONYMOUS) || routeRoles.contains(role)) {
            handler.handle(ctx);
          } else {
            throw new HttpResponseException(HttpStatus.FORBIDDEN.getCode());
          }
        });
      })
      .exception(HttpResponseException.class, App::handleException)
      .exception(Exception.class, App::handleException)
      .before(ctx -> {
        requestLogger.before(ctx);
        requestMonitor.before(ctx);
      })
      .after(ctx -> {
        requestLogger.after(ctx);
        requestMonitor.after(ctx);
      })
      .get("/actuator/health", ctx -> ctx.json(health))
      .get("/actuator/status", status::status, Role.SYSTEM)
      .get("/actuator/status/java", status::java, Role.SYSTEM)
      .get("/actuator/status/app", status::app, Role.SYSTEM)
      .get("/actuator/status/monitors", status::monitors, Role.SYSTEM)
      .get("/actuator/status/logs", status::logs, Role.SYSTEM)
      .get("/api/v1/hello", ctx -> ctx.result("Hello World"), Role.ANONYMOUS)
      .start(PORT);

    log.info("HTTP server started on port: " + PORT);
    log.info("App started in {}ms", System.currentTimeMillis() - START);

    Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
  }

  private static void handleException(Exception e, Context ctx) {
    if (e instanceof HttpResponseException e1) {
      ctx.status(e1.getStatus()).result(e.getMessage());
    } else {
      log.error("", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("internal server error");
    }
  }

  private static Role getUserRole(Context ctx) {
    Role role = Role.ANONYMOUS;
    var userApiKey = ctx.req().getHeader("X-API-KEY");
    if (userApiKey != null) {
      if (X_API_KEY.equals(userApiKey)) {
        role = Role.SYSTEM;
      } else {
        throw new HttpResponseException(HttpStatus.UNAUTHORIZED.getCode());
      }
    }
    return role;
  }

  public static void setRootLevel(Level level) {
    final ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    root.setLevel(level);
  }

  enum Role implements RouteRole {
    ANONYMOUS, SYSTEM
  }

  private record Health(String status) implements Serializable {
  }
}
