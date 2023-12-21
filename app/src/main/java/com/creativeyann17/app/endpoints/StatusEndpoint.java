package com.creativeyann17.app.endpoints;

import com.creativeyann17.app.App;
import com.creativeyann17.app.filters.SecurityFilter;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

@Aspect
@Component
@RestControllerEndpoint(id = "status")
@ConditionalOnProperty(value = "status.enabled", havingValue = "true")
@RequiredArgsConstructor
public class StatusEndpoint {

  private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
  private static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
  private static final String ROOT_PACKAGE = "com.creativeyann17.app";

  private final ApplicationContext ctx;
  private final SecurityFilter securityFilter;
  @Value("${spring.application.name:}")
  private String appName;
  @Value("${logging.file.name}")
  private String logFileName;
  @Value("${status.logs-size:100}")
  private long logsSize;

  private void checkIsSystem() {
    if (!securityFilter.isSystem()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

  @GetMapping(produces = "text/plain")
  public String status() {
    this.checkIsSystem();
    var monitor = MonitorFactory.start("StatusEndpoint.status()");  // monitor yourself
    String builder = "Java:\n" +
      "~~~~~\n" +
      java() +
      "App:\n" +
      "~~~~~\n" +
      app() +
      "Monitors:\n" +
      "~~~~~\n" +
      monitors() +
      "Logs:\n" +
      "~~~~~\n" +
      logs(null);
    monitor.stop();
    return builder;
  }

  @GetMapping(value = "/java", produces = "text/plain")
  public String java() {
    this.checkIsSystem();
    String builder = "Version: " + System.getProperty("java.version") + "\n" +
      "Home: " + System.getProperty("java.home") + "\n" +
      "Max memory: " + Runtime.getRuntime().maxMemory() + " (" + FileUtils.byteCountToDisplaySize(Runtime.getRuntime().maxMemory()) + ")\n" +
      "Free memory: " + Runtime.getRuntime().freeMemory() + " (" + FileUtils.byteCountToDisplaySize(Runtime.getRuntime().freeMemory()) + ")\n" +
      "Total memory: " + Runtime.getRuntime().totalMemory() + " (" + FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory()) + ")\n" +
      "Available processors: " + Runtime.getRuntime().availableProcessors() + "\n" +
      "\n";
    return builder;
  }

  @GetMapping(value = "/app", produces = "text/plain")
  public String app() {
    this.checkIsSystem();
    String builder = "Name: " + appName + "\n" +
      "Profiles: " + Arrays.toString(ctx.getEnvironment().getActiveProfiles()) + "\n" +
      "Server time: " + LocalDateTime.now().format(SIMPLE_FORMATTER) + "\n" +
      "Uptime: " + DurationFormatUtils.formatDuration(System.currentTimeMillis() - ctx.getStartupDate(), DATE_TIME_FORMAT, true) + "\n" +
      "\n";
    return builder;
  }

  @GetMapping(value = "/monitors", produces = "text/plain")
  public String monitors() {
    this.checkIsSystem();
    StringBuilder builder = new StringBuilder();
    var monitors = Arrays.stream(MonitorFactory.getRootMonitor().getMonitors())
      .filter((m) -> m.getHits() > 0)
      .sorted((m1, m2) -> Double.compare(m2.getTotal(), m1.getTotal()))
      .toList();
    int lm = monitors.stream().map(Monitor::getLabel).mapToInt(String::length).max().orElse(10);
    for (Monitor monitor : monitors) {
      builder.append(String.format("%-" + lm + "s -> %8.0f hits; %8.1f avg; %8.1f min; %8.1f max;\n", monitor.getLabel(),
        monitor.getHits(), monitor.getAvg(), monitor.getMin(), monitor.getMax()));
    }
    builder.append("\n");
    return builder.toString();
  }

  @GetMapping(value = "/logs", produces = "text/plain")
  public String logs(@RequestParam(value = "size", required = false) Long size) {
    this.checkIsSystem();
    var maxSize = Optional.ofNullable(size).orElse(logsSize);
    StringBuilder builder = new StringBuilder();
    try {
      var lines = Files.readAllLines(Path.of(logFileName));
      lines.stream().skip(Math.max(lines.size() - maxSize, 0)).forEach((l) -> builder.append(l + "\n"));
    } catch (Exception e) {
      builder.append("Failed to read logs: " + e.getMessage());
    }
    builder.append("\n");
    return builder.toString();
  }

  @Around("within(" + ROOT_PACKAGE + "..*)" +
    "&& !within(" + ROOT_PACKAGE + ".filters..*)" +
    "&& !within(" + ROOT_PACKAGE + ".endpoints..*)" +
    "&& !within(" + ROOT_PACKAGE + ".configurations..*)")
  public Object monitors(ProceedingJoinPoint joinPoint) throws Throwable {
    String targetClass = joinPoint.getTarget().getClass().getSimpleName();
    String targetMethod = joinPoint.getSignature().getName();
    var monitor = MonitorFactory.start(String.format("%s.%s()", targetClass, targetMethod));
    try {
      return joinPoint.proceed();
    } finally {
      monitor.stop();
    }
  }

}
