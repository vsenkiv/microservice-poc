package com.microservices.order.service;

import com.microservices.order.client.InventoryServiceClient;
import com.microservices.order.dto.CreateOrderRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

  private final InventoryServiceClient inventoryClient;

  @CircuitBreaker(name = "inventory-service", fallbackMethod = "fallbackInventoryCheck")
  @Retry(name = "inventory-service")
  public List<InventoryServiceClient.InventoryResponse> validateInventory(
      List<CreateOrderRequest.OrderItemRequest> items) {

    log.info("🔍 Checking inventory for {} items", items.size());

    try {
      List<InventoryServiceClient.InventoryResponse> inventoryChecks = items.stream()
          .map(item -> {
            log.debug("Checking inventory for productId: {}, quantity: {}",
                item.getProductId(), item.getQuantity());

            InventoryServiceClient.InventoryResponse inventory =
                inventoryClient.checkInventory(item.getProductId(), item.getQuantity());

            if (inventory == null) {
              throw new RuntimeException(
                  "No inventory response for product: " + item.getProductId());
            }

            if (!inventory.isAvailable()) {
              throw new RuntimeException("Product " + item.getProductId() +
                  " is not available in requested quantity. Available: " +
                  inventory.getAvailableQuantity() + ", Requested: " + item.getQuantity());
            }
            return inventory;
          })
          .collect(Collectors.toList());

      log.info("✅ Successfully validated inventory for all {} items", items.size());
      return inventoryChecks;

    } catch (Exception e) {
      log.error("❌ Error during inventory validation: {}", e.getMessage());
      throw e; // Let circuit breaker handle this
    }
  }

  // ✅ Fallback method with EXACT same signature
  public List<InventoryServiceClient.InventoryResponse> fallbackInventoryCheck(
      List<CreateOrderRequest.OrderItemRequest> items, Exception ex) {

    log.warn("🔄 FALLBACK TRIGGERED! Inventory service is unavailable, using fallback. Error: {}",
        ex.getMessage());

    List<InventoryServiceClient.InventoryResponse> fallbackResponses = items.stream()
        .map(item -> {
          InventoryServiceClient.InventoryResponse response = new InventoryServiceClient.InventoryResponse();
          response.setProductId(item.getProductId());
          response.setProductName("FALLBACK-Product-" + item.getProductId());
          response.setAvailableQuantity(1000);
          response.setAvailable(true);
          response.setPrice(99.99);

          log.info("🚨 Using FALLBACK inventory data for productId: {}", item.getProductId());
          return response;
        })
        .collect(Collectors.toList());

    log.info("🔄 Fallback returned {} items", fallbackResponses.size());
    return fallbackResponses;
  }
}