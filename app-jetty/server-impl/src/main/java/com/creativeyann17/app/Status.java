package com.creativeyann17.app;

import com.creativeyann17.server.context.Context;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
public class Status {

  private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
  private static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

  private final String appName;
  private final long start;

  public void status(Context ctx) {
    String builder = "Java:\n" +
      "~~~~~\n" +
      java(null) +
      "App:\n" +
      "~~~~~\n" +
      app(null) +
      "Monitors:\n" +
      "~~~~~\n" +
      monitors(null) +
      "Logs:\n" +
      "~~~~~\n" +
      logs(null);
    ctx.text(builder);
  }

  public String java(Context ctx) {
    String builder = "Version: " + System.getProperty("java.version") + "\n" +
      "Max memory: " + FileUtils.byteCountToDisplaySize(Runtime.getRuntime().maxMemory()).replace(" ", "") + " CPUs: " + Runtime.getRuntime().availableProcessors() + "\n" +
      "\n";
    if (ctx != null)
      ctx.text(builder);
    return builder;
  }

  public String app(Context ctx) {
    String builder = "Name: " + appName + "\n" +
      "Server time: " + LocalDateTime.now().format(SIMPLE_FORMATTER) + "\n" +
      "Uptime: " + DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, DATE_TIME_FORMAT, true) + "\n" +
      "\n";
    if (ctx != null)
      ctx.text(builder);
    return builder;
  }

  public String monitors(Context ctx) {
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
    if (ctx != null)
      ctx.text(builder.toString());
    return builder.toString();
  }

  public String logs(Context ctx) {
    var maxSize = Optional.ofNullable(ctx).map(c -> c.request().queryParam("size")).map(Long::parseLong).orElse(50L);
    StringBuilder builder = new StringBuilder();
    try {
      var lines = Files.readAllLines(Path.of("./logs/app.log"));
      lines.stream().skip(Math.max(lines.size() - maxSize, 0)).forEach((l) -> builder.append(l + "\n"));
    } catch (Exception e) {
      builder.append("Failed to read logs: " + e.getMessage());
    }
    builder.append("\n");
    if (ctx != null)
      ctx.text(builder.toString());
    return builder.toString();
  }
}
