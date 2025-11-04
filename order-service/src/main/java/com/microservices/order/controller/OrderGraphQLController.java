package com.microservices.order.controller;

import com.microservices.order.client.UserServiceClient;
import com.microservices.order.dto.OrderResponse;
import com.microservices.order.service.OrderService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
public class OrderGraphQLController {

  private final OrderService orderService;
  private final UserServiceClient userServiceClient;

  // Add this constructor to verify bean creation
  public OrderGraphQLController(OrderService orderService, UserServiceClient userServiceClient) {
    this.orderService = orderService;
    this.userServiceClient = userServiceClient;
    log.info("🚀 OrderGraphQLController initialized successfully!");
  }

  @QueryMapping
  public Mono<OrderResponse> getOrder(@Argument Long orderId) {
    log.info("GraphQL: Fetching order with ID: {}", orderId);

    return Mono.fromCallable(() -> {
      try {
        return orderService.getOrderById(orderId);
      } catch (Exception e) {
        log.error("Error fetching order {}: {}", orderId, e.getMessage());
        throw new RuntimeException("Order not found: " + orderId, e);
      }
    });
  }

  @QueryMapping
  public Mono<List<OrderResponse>> getOrders(@Argument Long userId) {
    log.info("GraphQL: Fetching orders for user: {}", userId);

    return Mono.fromCallable(() -> {
      try {
        return orderService.getOrdersByUserId(userId);
      } catch (Exception e) {
        log.error("Error fetching orders for user {}: {}", userId, e.getMessage());
        throw new RuntimeException("Orders not found for user: " + userId, e);
      }
    });
  }

  // Field resolver for user - only called if 'user' field is requested
  @SchemaMapping(typeName = "OrderResponse", field = "user")
  public Mono<UserServiceClient.UserResponse> getUser(OrderResponse orderResponse) {
    log.info("GraphQL: Fetching user details for userId: {}", orderResponse.getUserId());

    if (orderResponse.getUserId() == null) {
      return Mono.empty();
    }

    return Mono.fromCallable(() -> {
      try {
        return orderService.getUserWithCache(orderResponse.getUserId());
      } catch (Exception e) {
        log.error("Error fetching user {}: {}", orderResponse.getUserId(), e.getMessage());
        return null; // Return null instead of throwing to allow partial data
      }
    });
  }
}