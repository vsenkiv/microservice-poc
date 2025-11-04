package com.microservices.order.service;

import com.microservices.order.client.InventoryServiceClient;
import com.microservices.order.client.PaymentServiceClient;
import com.microservices.order.client.UserServiceClient;
import com.microservices.order.dto.CreateOrderRequest;
import com.microservices.order.dto.OrderResponse;
import com.microservices.order.entity.Order;
import com.microservices.order.entity.OrderItem;
import com.microservices.order.entity.OrderStatus;
import com.microservices.order.repository.OrderRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class OrderService {

  private final OrderRepository orderRepository;
  private final InventoryService inventoryValidationService;
  private final UserServiceClient userClient;
  private final PaymentServiceClient paymentClient;
  private final Tracer tracer;
  private OrderEventPublisher orderEventPublisher;

  // Add this method to OrderService.java
  @Cacheable(value = "userOrders", key = "#userId", unless = "#result == null or #result.isEmpty()")
  public List<OrderResponse> getOrdersByUserId(Long userId) {
    log.info("🔍 Fetching orders from database (cache miss) for userId: {}", userId);

    List<Order> orders = orderRepository.findByUserIdOrderByIdDesc(userId);

    if (orders.isEmpty()) {
      log.info("No orders found for user: {}", userId);
      return List.of();
    }

    // Get user info once and reuse
    UserServiceClient.UserResponse user = getUserWithCache(userId);

    List<OrderResponse> responses = orders.stream()
        .map(order -> buildOrderResponse(order, user, null, null))
        .collect(Collectors.toList());

    log.info("✅ {} orders retrieved from database and cached for user: {}", orders.size(), userId);
    return responses;
  }

  // ✅ Cache order retrieval
  @Cacheable(value = "orders", key = "#orderId", unless = "#result == null")
  public OrderResponse getOrderById(Long orderId) {
    log.info("🔍 Fetching order from database (cache miss) for orderId: {}", orderId);

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

    UserServiceClient.UserResponse user = getUserWithCache(order.getUserId());
    OrderResponse response = buildOrderResponse(order, user, null, null);

    log.info("✅ Order {} retrieved from database and cached", orderId);
    return response;
  }

  // ✅ Cache user information
  @Cacheable(value = "users", key = "#userId", unless = "#result == null")
  public UserServiceClient.UserResponse getUserWithCache(Long userId) {
    log.info("🔍 Fetching user from service (cache miss) for userId: {}", userId);
    UserServiceClient.UserResponse user = userClient.getUserById(userId);
    log.info("✅ User {} retrieved from service and cached", userId);
    return user;
  }

  // ✅ Evict cache when order is created/updated
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "orders", key = "#result.id", condition = "#result != null"),
      @CacheEvict(value = "users", key = "#request.userId")
  })
  public OrderResponse createOrder(CreateOrderRequest request) {
    Span span = tracer.spanBuilder("create-order")
        .setAttribute("user.id", request.getUserId())
        .setAttribute("items.count", request.getItems().size())
        .startSpan();

    try {
      log.info("🛒 Creating new order for user: {}", request.getUserId());

      // 1. Validate user exists (this will cache the user if not already cached)
      UserServiceClient.UserResponse user = getUserWithCache(request.getUserId());
      if (user == null) {
        throw new RuntimeException("User not found with ID: " + request.getUserId());
      }

      // 2. Use separate service for circuit breaker
      log.info("📦 Starting inventory validation for order...");
      List<InventoryServiceClient.InventoryResponse> inventoryChecks =
          inventoryValidationService.validateInventory(request.getItems());

      // 3. Create order
      BigDecimal totalAmount = calculateTotalAmount(request.getItems(), inventoryChecks);

      Order order = orderRepository.save(
          new Order(request.getUserId(), totalAmount, OrderStatus.PENDING));

      // 4. Create order items
      List<OrderItem> orderItems = createOrderItems(request.getItems(), inventoryChecks, order);
      order.setOrderItems(orderItems);
      order = orderRepository.save(order);

      // 5. Update order status
      order.setStatus(OrderStatus.CONFIRMED);
      order = orderRepository.save(order);

      // Publish event to Kafka
      orderEventPublisher.publishOrderCreatedEvent(order, user.getEmail());

      // 6. Build response
      OrderResponse response = buildOrderResponse(order, user, inventoryChecks, "CONFIRMED");

      log.info("✅ Order created successfully with ID: {}, cache evicted for future consistency",
          order.getId());
      return response;

    } finally {
      span.end();
    }
  }

  // ✅ Method to manually evict cache (useful for testing)
  @CacheEvict(value = {"orders", "users"}, allEntries = true)
  public void evictAllCaches() {
    log.info("🗑️ All caches evicted manually");
  }

  // ✅ Method to evict specific order cache
  @CacheEvict(value = "orders", key = "#orderId")
  public void evictOrderCache(Long orderId) {
    log.info("🗑️ Cache evicted for orderId: {}", orderId);
  }

  private BigDecimal calculateTotalAmount(List<CreateOrderRequest.OrderItemRequest> items,
      List<InventoryServiceClient.InventoryResponse> inventoryChecks) {
    return inventoryChecks.stream()
        .map(inv -> BigDecimal.valueOf(inv.getPrice())
            .multiply(BigDecimal.valueOf(items.stream()
                .filter(item -> item.getProductId().equals(inv.getProductId()))
                .findFirst()
                .map(CreateOrderRequest.OrderItemRequest::getQuantity)
                .orElse(0))))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private List<OrderItem> createOrderItems(List<CreateOrderRequest.OrderItemRequest> items,
      List<InventoryServiceClient.InventoryResponse> inventoryChecks,
      Order order) {
    return items.stream()
        .map(itemReq -> {
          InventoryServiceClient.InventoryResponse inventory = inventoryChecks.stream()
              .filter(inv -> inv.getProductId().equals(itemReq.getProductId()))
              .findFirst()
              .orElseThrow(() -> new RuntimeException(
                  "Inventory data not found for product: " + itemReq.getProductId()));

          return new OrderItem(null, order, itemReq.getProductId(),
              itemReq.getQuantity(), BigDecimal.valueOf(inventory.getPrice()),
              BigDecimal.valueOf(itemReq.getQuantity())
                  .multiply(BigDecimal.valueOf(inventory.getPrice())));
        })
        .collect(Collectors.toList());
  }

  private OrderResponse buildOrderResponse(Order order, UserServiceClient.UserResponse user,
      List<InventoryServiceClient.InventoryResponse> inventoryData,
      String paymentStatus) {
    OrderResponse response = new OrderResponse();
    response.setId(String.valueOf(order.getId()));
    Optional.ofNullable(user).ifPresent(u -> {
      response.setUserId(u.getId());
      response.setUserName(u.getUsername());
    });

    response.setTotalAmount(order.getTotalAmount());
    response.setStatus(order.getStatus().name());
    Optional.ofNullable(paymentStatus).ifPresent(response::setPaymentStatus);

    List<OrderResponse.OrderItemResponse> itemResponses = order.getOrderItems().stream()
        .map(item -> {
          OrderResponse.OrderItemResponse itemResp = new OrderResponse.OrderItemResponse();
          itemResp.setProductId(item.getProductId());
          itemResp.setQuantity(item.getQuantity());
          itemResp.setUnitPrice(item.getUnitPrice());
          itemResp.setTotalPrice(item.getTotalPrice());

          Optional.ofNullable(inventoryData).ifPresent(invList -> {
            invList.stream()
                .filter(inv -> inv.getProductId().equals(item.getProductId()))
                .findFirst()
                .ifPresent(inv -> itemResp.setProductName(inv.getProductName()));
          });

          return itemResp;
        })
        .collect(Collectors.toList());

    response.setItems(itemResponses);
    return response;
  }
}