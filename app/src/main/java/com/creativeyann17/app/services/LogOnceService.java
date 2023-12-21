package com.creativeyann17.app.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LogOnceService {

  private final List<String> logs = new ArrayList<>();

  @Scheduled(fixedRateString = "${security.eviction}", timeUnit = TimeUnit.MINUTES)
  public synchronized void eviction() {
    logs.clear();
  }

  public synchronized void warn(String msg) {
    if (!logs.contains(msg)) {
      log.warn(msg);
      logs.add(msg);
    }
  }
}
