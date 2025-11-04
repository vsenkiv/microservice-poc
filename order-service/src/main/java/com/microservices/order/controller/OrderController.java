package com.microservices.order.controller;

import com.microservices.common.commands.CreateOrderCommand;
import com.microservices.order.dto.CreateOrderRequest;
import com.microservices.order.dto.OrderResponse;
import com.microservices.order.security.AuthenticationFacade;
import com.microservices.order.service.OrderService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@Slf4j
@AllArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final AuthenticationFacade authenticationFacade;
  private final CommandGateway commandGateway;

  @GetMapping("/me")
  public ResponseEntity<Map<String, Object>> getCurrentUser(
      @AuthenticationPrincipal OidcUser principal) {
    log.info("Fetching current user info");

    if (principal == null) {
      return ResponseEntity.ok(Map.of("authenticated", false));
    }

    return ResponseEntity.ok(Map.of(
        "authenticated", true,
        "email", principal.getEmail(),
        "name", principal.getFullName(),
        "picture", principal.getPicture(),
        "sub", principal.getSubject()
    ));
  }

  @PostMapping("/saga")
  public ResponseEntity<OrderResponse> createOrderWithSaga(
      @Valid @RequestBody CreateOrderRequest request) {
    log.info("Received SAGA order creation request for user: {} with {} items",
        request.getUserId(), request.getItems().size());

    try {
      Random random = new Random();
      String orderId = String.valueOf(random.nextLong());

      // Convert request items to command items
      var commandItems = request.getItems().stream()
          .map(item -> new CreateOrderCommand.OrderItemRequest(
              item.getProductId(),
              item.getQuantity(),
              BigDecimal.TWO))
          .collect(Collectors.toList());

      CreateOrderCommand command = new CreateOrderCommand(
          orderId,
          request.getUserId(),
          commandItems,
          "USD",
          "CREDIT CARD"
      );

      // Send command to aggregate - this will start the saga
      commandGateway.send(command);

      log.info("Successfully started order processing saga with orderId: {} for user: {}",
          orderId, request.getUserId());

      return ResponseEntity.accepted().body(
          OrderResponse.builder()
              .id(orderId)
              .userId(request.getUserId())
              .status("STARTED")
              .build());
    } catch (Exception e) {
      log.error("Failed to start order processing saga for user: {} - Error: {}",
          request.getUserId(), e.getMessage(), e);
      throw e;
    }
  }

  @PostMapping
  public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    log.info("Received order creation request for user: {} with {} items",
        request.getUserId(), request.getItems().size());

    try {
      OrderResponse response = orderService.createOrder(request);
      log.info("Successfully created order with ID: {} for user: {}",
          response.getId(), request.getUserId());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Failed to create order for user: {} - Error: {}",
          request.getUserId(), e.getMessage(), e);
      throw e;
    }
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
    log.info("Fetching order with ID: {}", orderId);
    return ResponseEntity.ok(orderService.getOrderById(orderId));
  }
}