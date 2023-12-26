package com.creativeyann17.server;

import com.creativeyann17.server.context.Context;
import com.creativeyann17.server.handlers.AccessHandler;
import com.creativeyann17.server.handlers.GlobalHandler;
import com.creativeyann17.server.handlers.RoutesHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Server {

  private final org.eclipse.jetty.server.Server server;
  private final GlobalHandler globalHandler;
  private final ObjectMapper objectMapper;
  private boolean isStarted = false;

  private Server(int port) {
    QueuedThreadPool threadPool = new QueuedThreadPool();
    threadPool.setVirtualThreadsExecutor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("virtual-", 0).factory()));
    threadPool.setName("server");

    this.server = new org.eclipse.jetty.server.Server(threadPool);
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);
    server.addConnector(connector);

    this.objectMapper = new ObjectMapper();
    this.globalHandler = new GlobalHandler(objectMapper);
    server.setHandler(globalHandler);
  }

  public static Server create(int port) {
    return new Server(port);
  }

  public Server jsonMapper(Consumer<ObjectMapper> mapper) {
    mapper.accept(this.objectMapper);
    return this;
  }

  public Server router(Consumer<RoutesHandler> router) {
    router.accept(this.globalHandler.getRoutesHandler());
    return this;
  }

  public Server before(Consumer<Context> handler) {
    this.globalHandler.setBefore(handler);
    return this;
  }

  public Server after(Consumer<Context> handler) {
    this.globalHandler.setAfter(handler);
    return this;
  }

  public Server access(AccessHandler accessHandler) {
    this.globalHandler.setAccessHandler(accessHandler);
    return this;
  }

  public Server start() {
    if (!isStarted) {
      try {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
          try {
            server.stop();
          } catch (Exception e) {
            System.err.println(e.getMessage());
          }
        }));
        isStarted = true;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return this;
  }
}
