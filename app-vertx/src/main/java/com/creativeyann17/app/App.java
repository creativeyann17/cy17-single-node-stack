package com.creativeyann17.app;

import ch.qos.logback.classic.Level;
import io.vertx.core.*;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import lombok.extern.slf4j.Slf4j;

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

  static {
    System.setProperty("vertxweb.environment", ENV);
    System.setProperty("org.vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
    var root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    root.setLevel("prod".equals(ENV) ? Level.ERROR : Level.INFO);
  }

  public static void main(String[] args) {

    log.info("App is starting with Cores: {}", Runtime.getRuntime().availableProcessors());
    log.info("Current profile: {}", ENV);

    var options = new VertxOptions();//.setClusterManager(new InfinispanClusterManager());
    Vertx vertx = Vertx.vertx(options);
    startVerticles(vertx);
    /*
          handleStartingFailure(Vertx.clusteredVertx(options)
        .onSuccess(App::startVerticles));
     */
  }

  private static void startVerticles(Vertx vertx) {
    handleStartingFailure(
      Future.all(
          vertx.deployVerticle(new HelloConsumer(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)),
          vertx.deployVerticle(new WebServer()))
        .onSuccess((all) -> {
          log.info("App started in {}ms", System.currentTimeMillis() - START);
        }));
  }

  private static void handleStartingFailure(Future<?> verticle) {
    verticle.onFailure(failure -> {
      log.error("", failure);
      System.exit(-1);
    });
  }

}
