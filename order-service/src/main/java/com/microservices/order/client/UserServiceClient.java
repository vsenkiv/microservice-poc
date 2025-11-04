package com.microservices.order.client;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@AllArgsConstructor
public class UserServiceClient {

  private final RestClient restClient;
  private final Tracer tracer;

  public UserResponse getUserById(Long userId) {
    Span span = tracer.spanBuilder("user-lookup")
        .setAttribute("user.id", userId)
        .startSpan();

    try {
      return restClient.get()
          .uri("/api/users/{userId}", userId)
          .retrieve()
          .body(UserResponse.class);
    } finally {
      span.end();
    }
  }

  @Data
  public static class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
  }
}