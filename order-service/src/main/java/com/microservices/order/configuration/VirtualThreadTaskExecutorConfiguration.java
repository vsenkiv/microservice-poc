package com.microservices.order.configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("virtual-threads")
public class VirtualThreadTaskExecutorConfiguration {

  @Bean
  public Executor taskExecutor(TaskExecutionProperties properties) {
    // Use a virtual thread executor
    return Executors.newVirtualThreadPerTaskExecutor();

  }
}
