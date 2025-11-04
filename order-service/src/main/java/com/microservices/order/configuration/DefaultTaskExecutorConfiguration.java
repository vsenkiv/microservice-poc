package com.microservices.order.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@Profile({"default", "local"})
public class DefaultTaskExecutorConfiguration {

  @Bean
  public ThreadPoolTaskExecutor defaultTaskExecutor() {
    System.out.println("Using Default Thread Pool for Task Execution");

    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(8);
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("Default-Executor-");
    executor.initialize(); // Initialize the executor
    return executor;
  }

}
