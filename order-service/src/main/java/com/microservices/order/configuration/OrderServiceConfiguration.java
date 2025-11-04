package com.microservices.order.configuration;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OrderServiceConfiguration {

  @Bean
  public Tracer tracer(OpenTelemetry openTelemetry) {
    return openTelemetry.getTracer("order-service", "1.0.0");
  }

  @Bean
  public RestClient restClient(RestClient.Builder builder,
      @Value("${user.service.url}") String userServiceUrl) {
    return builder
        .baseUrl(userServiceUrl)
        .build();
  }

  @Bean
  public RestClient.Builder restClientBuilder() {
    return RestClient.builder()
        .defaultHeader("Content-Type", "application/json") // Set a default header for all requests
        .defaultHeader("Accept", "application/json");
  }


}
