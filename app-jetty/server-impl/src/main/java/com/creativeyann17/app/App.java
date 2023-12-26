package com.creativeyann17.app;

import ch.qos.logback.classic.Level;
import com.creativeyann17.server.HttpResponseException;
import com.creativeyann17.server.Server;
import com.creativeyann17.server.context.Context;
import com.creativeyann17.server.handlers.RouteRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
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
    final ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    root.setLevel("prod".equals(ENV) ? Level.ERROR : Level.INFO);
  }

  public static void main(String[] args) {

    log.info("App is starting with Cores: {}", Runtime.getRuntime().availableProcessors());
    log.info("Current profile: {}", ENV);
    Server.create(PORT)
      .jsonMapper(mapper -> {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
      })
      .before(ctx -> {
        requestLogger.before(ctx);
        requestMonitor.before(ctx);
      })
      .access(App::getUserRoles)
      .router(routes -> {
        routes
          .subRoute("/actuator", Role.SYSTEM)
          .add(HttpMethod.GET, "/health", ctx -> ctx.json(health), Role.ANONYMOUS)
          .add(HttpMethod.GET, "/status", status::status)
          .add(HttpMethod.GET, "/status/java", status::java)
          .add(HttpMethod.GET, "/status/app", status::app)
          .add(HttpMethod.GET, "/status/monitors", status::monitors)
          .add(HttpMethod.GET, "/status/logs", status::logs);
        routes
          .add(HttpMethod.GET, "/api/v1/param/:param", ctx -> {
            ctx.text(ctx.request().pathParam("param"));
          })
          .add(HttpMethod.GET, "/api/v1/hello", ctx -> ctx.text("Hello World!"));
      })
      .after(ctx -> {
        requestLogger.after(ctx);
        requestMonitor.after(ctx);
      })
      .start();

    log.info("HTTP server started on port: " + PORT);
    log.info("App started in {}ms", System.currentTimeMillis() - START);
  }

  private static List<Role> getUserRoles(Context ctx) {
    var roles = new ArrayList<Role>();
    roles.add(Role.ANONYMOUS);
    var userApiKey = ctx.request().header("X-API-KEY");
    if (userApiKey != null) {
      if (X_API_KEY.equals(userApiKey)) {
        roles.add(Role.SYSTEM);
      } else {
        throw new HttpResponseException(HttpStatus.UNAUTHORIZED_401);
      }
    }
    return roles;
  }

  enum Role implements RouteRole {
    ANONYMOUS, SYSTEM
  }

  private record Health(String status) implements Serializable {
  }

  /*
   public static <U> U get(Supplier<U> f) {
    try {
      return f.get();
    } catch(NullPointerException | ArrayIndexOutOfBoundsException e) {
      return null;
    }
  }
   */
}
