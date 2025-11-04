package com.microservices.order.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MetricsController {

  private final Counter testCounter;
  private final Timer testTimer;

  public MetricsController(MeterRegistry meterRegistry) {
    this.testCounter = Counter.builder("test_requests_total")
        .description("Total test requests")
        .tag("service", "order-service")
        .register(meterRegistry);

    this.testTimer = Timer.builder("test_request_duration_seconds")
        .description("Test request duration")
        .tag("service", "order-service")
        .register(meterRegistry);
  }

  @GetMapping("metrics")
  public String testMetrics() throws Exception {
    return testTimer.recordCallable(() -> {
      testCounter.increment();
      // Simulate some work
      Thread.sleep(ThreadLocalRandom.current().nextInt(10, 100));
      return "Metrics test completed. Counter: " + testCounter.count();
    });
  }

}
