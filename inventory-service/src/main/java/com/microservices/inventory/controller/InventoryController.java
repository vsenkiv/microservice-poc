package com.microservices.inventory.controller;

import com.microservices.inventory.dto.CreateProductRequest;
import com.microservices.inventory.dto.InventoryResponse;
import com.microservices.inventory.dto.ProductResponse;
import com.microservices.inventory.dto.ReservationRequest;
import com.microservices.inventory.dto.StockAdjustmentRequest;
import com.microservices.inventory.dto.UpdateProductRequest;
import com.microservices.inventory.service.InventoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

  private final InventoryService inventoryService;

  @GetMapping("/check/{productId}")
  public ResponseEntity<InventoryResponse> checkInventory(
      @PathVariable Long productId,
      @RequestParam Integer quantity) {
    log.info("Checking inventory for product ID: {} with quantity: {}", productId, quantity);

    InventoryResponse response = inventoryService.checkInventory(productId, quantity);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/products")
  public ResponseEntity<ProductResponse> createProduct(
      @Valid @RequestBody CreateProductRequest request) {
    ProductResponse response = inventoryService.createProduct(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/products/{productId}")
  public ResponseEntity<ProductResponse> getProduct(@PathVariable Long productId) {
    log.info("Fetching product with ID: {}", productId);

    ProductResponse response = inventoryService.getProduct(productId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/products")
  public ResponseEntity<List<ProductResponse>> getAllProducts() {
    log.info("Fetching all products");

    List<ProductResponse> products = inventoryService.getAllProducts();
    return ResponseEntity.ok(products);
  }

  @GetMapping("/products/category/{category}")
  public ResponseEntity<List<ProductResponse>> getProductsByCategory(
      @PathVariable String category) {
    log.info("Fetching products by category: {}", category);

    List<ProductResponse> products = inventoryService.getProductsByCategory(category);
    return ResponseEntity.ok(products);
  }

  @GetMapping("/products/search")
  public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String keyword) {
    log.info("Searching products with keyword: {}", keyword);

    List<ProductResponse> products = inventoryService.searchProducts(keyword);
    return ResponseEntity.ok(products);
  }

  @PutMapping("/products/{productId}")
  public ResponseEntity<ProductResponse> updateProduct(
      @PathVariable Long productId,
      @Valid @RequestBody UpdateProductRequest request) {
    log.info("Updating product with ID: {}", productId);

    ProductResponse response = inventoryService.updateProduct(productId, request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/products/{productId}/adjust")
  public ResponseEntity<ProductResponse> adjustStock(
      @PathVariable Long productId,
      @Valid @RequestBody StockAdjustmentRequest request) {
    log.info("Adjusting stock for product ID: {}", productId);

    ProductResponse response = inventoryService.adjustStock(productId, request, "vsenkiv");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/reservations")
  public ResponseEntity<Void> reserveStock(@Valid @RequestBody ReservationRequest request) {
    log.info("Reserving stock for order ID: {}, product ID: {}",
        request.getOrderId(), request.getProductId());

    inventoryService.reserveStock(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping("/reservations/order/{orderId}/product/{productId}")
  public ResponseEntity<Void> releaseReservation(
      @PathVariable Long orderId,
      @PathVariable Long productId) {
    log.info("Releasing reservation for order ID: {}, product ID: {}", orderId, productId);

    inventoryService.releaseReservation(orderId, productId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/reservations/order/{orderId}/product/{productId}/fulfill")
  public ResponseEntity<Void> fulfillReservation(
      @PathVariable Long orderId,
      @PathVariable Long productId) {
    log.info("Fulfilling reservation for order ID: {}, product ID: {}", orderId, productId);

    inventoryService.fulfillReservation(orderId, productId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/products/reorder")
  public ResponseEntity<List<ProductResponse>> getProductsNeedingReorder() {
    log.info("Fetching products needing reorder");

    List<ProductResponse> products = inventoryService.getProductsNeedingReorder();
    return ResponseEntity.ok(products);
  }
}