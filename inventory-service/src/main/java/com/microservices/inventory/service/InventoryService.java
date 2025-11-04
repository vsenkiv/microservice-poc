package com.microservices.inventory.service;

import com.microservices.inventory.dto.CreateProductRequest;
import com.microservices.inventory.dto.InventoryResponse;
import com.microservices.inventory.dto.ProductResponse;
import com.microservices.inventory.dto.ReservationRequest;
import com.microservices.inventory.dto.StockAdjustmentRequest;
import com.microservices.inventory.dto.UpdateProductRequest;
import com.microservices.inventory.entity.InventoryTransaction;
import com.microservices.inventory.entity.Product;
import com.microservices.inventory.entity.StockReservation;
import com.microservices.inventory.exception.InsufficientStockException;
import com.microservices.inventory.exception.ProductNotFoundException;
import com.microservices.inventory.repository.InventoryTransactionRepository;
import com.microservices.inventory.repository.ProductRepository;
import com.microservices.inventory.repository.StockReservationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

  private final ProductRepository productRepository;
  private final InventoryTransactionRepository transactionRepository;
  private final StockReservationRepository reservationRepository;

  @Transactional(readOnly = true)
  public InventoryResponse checkInventory(Long productId, Integer quantity) {
    log.info("Checking inventory for product ID: {} with quantity: {}", productId, quantity);

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

    boolean available = product.isAvailable(quantity);

    return InventoryResponse.builder()
        .productId(product.getId())
        .productName(product.getName())
        .availableQuantity(product.getAvailableQuantity())
        .available(available)
        .price(product.getPrice())
        .build();
  }

  @Transactional
  public ProductResponse createProduct(CreateProductRequest request) {
    log.info("Creating product with SKU: {}", request.getSku());

    if (productRepository.existsBySku(request.getSku())) {
      throw new IllegalArgumentException("Product with SKU already exists: " + request.getSku());
    }

    Product product = Product.builder()
        .sku(request.getSku())
        .name(request.getName())
        .description(request.getDescription())
        .category(request.getCategory())
        .price(request.getPrice())
        .quantity(request.getQuantity())
        .reservedQuantity(0)
        .reorderLevel(request.getReorderLevel() != null ? request.getReorderLevel() : 10)
        .reorderQuantity(request.getReorderQuantity() != null ? request.getReorderQuantity() : 50)
        .status(Product.ProductStatus.ACTIVE)
        .imageUrl(request.getImageUrl())
        .weight(request.getWeight() != null ? request.getWeight() : 0.0)
        .weightUnit(request.getWeightUnit() != null ? request.getWeightUnit() : "kg")
        .build();

    Product savedProduct = productRepository.save(product);

    // Create initial transaction
    createTransaction(savedProduct, InventoryTransaction.TransactionType.RESTOCK,
        request.getQuantity(), 0, request.getQuantity(), null, "INITIAL_STOCK",
        "Initial stock creation", "SYSTEM");

    log.info("Successfully created product with ID: {}", savedProduct.getId());
    return mapToProductResponse(savedProduct);
  }

  @Transactional(readOnly = true)
  public ProductResponse getProduct(Long productId) {
    log.info("Fetching product with ID: {}", productId);

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

    return mapToProductResponse(product);
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> getAllProducts() {
    log.info("Fetching all products");
    return productRepository.findAll().stream()
        .map(this::mapToProductResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> getProductsByCategory(String category) {
    log.info("Fetching products by category: {}", category);
    return productRepository.findByCategory(category).stream()
        .map(this::mapToProductResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> searchProducts(String keyword) {
    log.info("Searching products with keyword: {}", keyword);
    return productRepository.searchProducts(keyword).stream()
        .map(this::mapToProductResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public ProductResponse updateProduct(Long productId, UpdateProductRequest request) {
    log.info("Updating product with ID: {}", productId);

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

    if (request.getName() != null) {
      product.setName(request.getName());
    }
    if (request.getDescription() != null) {
      product.setDescription(request.getDescription());
    }
    if (request.getCategory() != null) {
      product.setCategory(request.getCategory());
    }
    if (request.getPrice() != null) {
      product.setPrice(request.getPrice());
    }
    if (request.getReorderLevel() != null) {
      product.setReorderLevel(request.getReorderLevel());
    }
    if (request.getReorderQuantity() != null) {
      product.setReorderQuantity(request.getReorderQuantity());
    }
    if (request.getImageUrl() != null) {
      product.setImageUrl(request.getImageUrl());
    }
    if (request.getWeight() != null) {
      product.setWeight(request.getWeight());
    }
    if (request.getWeightUnit() != null) {
      product.setWeightUnit(request.getWeightUnit());
    }

    Product savedProduct = productRepository.save(product);
    log.info("Successfully updated product with ID: {}", productId);

    return mapToProductResponse(savedProduct);
  }

  @Transactional
  public ProductResponse adjustStock(Long productId, StockAdjustmentRequest request,
      String performedBy) {
    log.info("Adjusting stock for product ID: {} by {} units", productId, request.getQuantity());

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

    int previousQuantity = product.getQuantity();
    int newQuantity = previousQuantity + request.getQuantity();

    if (newQuantity < product.getReservedQuantity()) {
      throw new IllegalArgumentException(
          "Cannot reduce stock below reserved quantity. Reserved: "
              + product.getReservedQuantity());
    }

    product.setQuantity(newQuantity);

    // Update status if out of stock
    if (product.getAvailableQuantity() <= 0) {
      product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
    } else if (product.getStatus() == Product.ProductStatus.OUT_OF_STOCK) {
      product.setStatus(Product.ProductStatus.ACTIVE);
    }

    Product savedProduct = productRepository.save(product);

    // Create transaction
    InventoryTransaction.TransactionType transactionType =
        InventoryTransaction.TransactionType.valueOf(request.getAdjustmentType().toUpperCase());

    createTransaction(savedProduct, transactionType, request.getQuantity(),
        previousQuantity, newQuantity, null, request.getAdjustmentType(),
        request.getNotes(), performedBy);

    log.info("Successfully adjusted stock for product ID: {}. New quantity: {}", productId,
        newQuantity);

    return mapToProductResponse(savedProduct);
  }

  @Transactional
  public void reserveStock(ReservationRequest request) {
    log.info("Reserving stock for order ID: {}, product ID: {}, quantity: {}",
        request.getOrderId(), request.getProductId(), request.getQuantity());

    Product product = productRepository.findById(request.getProductId())
        .orElseThrow(() -> new ProductNotFoundException(
            "Product not found with ID: " + request.getProductId()));

    if (!product.isAvailable(request.getQuantity())) {
      throw new InsufficientStockException(
          String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
              product.getName(), product.getAvailableQuantity(), request.getQuantity()));
    }

    // Create reservation
    int expirationMinutes =
        request.getExpirationMinutes() != null ? request.getExpirationMinutes() : 15;
    StockReservation reservation = StockReservation.builder()
        .product(product)
        .orderId(request.getOrderId())
        .quantity(request.getQuantity())
        .status(StockReservation.ReservationStatus.ACTIVE)
        .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
        .build();

    reservationRepository.save(reservation);

    // Update reserved quantity
    product.setReservedQuantity(product.getReservedQuantity() + request.getQuantity());
    productRepository.save(product);

    // Create transaction
    createTransaction(product, InventoryTransaction.TransactionType.RESERVE,
        request.getQuantity(), product.getQuantity(), product.getQuantity(),
        request.getOrderId(), "ORDER", "Stock reserved for order", "SYSTEM");

    log.info("Successfully reserved {} units for order ID: {}", request.getQuantity(),
        request.getOrderId());
  }

  @Transactional
  public void releaseReservation(Long orderId, Long productId) {
    log.info("Releasing reservation for order ID: {}, product ID: {}", orderId, productId);

    StockReservation reservation = reservationRepository.findByOrderIdAndProductId(orderId,
            productId)
        .orElseThrow(() -> new IllegalArgumentException(
            "No active reservation found for order: " + orderId + ", product: " + productId));

    if (reservation.getStatus() != StockReservation.ReservationStatus.ACTIVE) {
      throw new IllegalArgumentException("Reservation is not active: " + reservation.getStatus());
    }

    Product product = reservation.getProduct();
    product.setReservedQuantity(product.getReservedQuantity() - reservation.getQuantity());
    productRepository.save(product);

    reservation.setStatus(StockReservation.ReservationStatus.CANCELLED);
    reservationRepository.save(reservation);

    // Create transaction
    createTransaction(product, InventoryTransaction.TransactionType.RELEASE,
        reservation.getQuantity(), product.getQuantity(), product.getQuantity(),
        orderId, "ORDER", "Reservation released", "SYSTEM");

    log.info("Successfully released reservation for order ID: {}", orderId);
  }

  @Transactional
  public void fulfillReservation(Long orderId, Long productId) {
    log.info("Fulfilling reservation for order ID: {}, product ID: {}", orderId, productId);

    StockReservation reservation = reservationRepository.findByOrderIdAndProductId(orderId,
            productId)
        .orElseThrow(() -> new IllegalArgumentException(
            "No active reservation found for order: " + orderId + ", product: " + productId));

    if (reservation.getStatus() != StockReservation.ReservationStatus.ACTIVE) {
      throw new IllegalArgumentException("Reservation is not active: " + reservation.getStatus());
    }

    Product product = reservation.getProduct();

    // Reduce actual stock and reserved quantity
    product.setQuantity(product.getQuantity() - reservation.getQuantity());
    product.setReservedQuantity(product.getReservedQuantity() - reservation.getQuantity());

    // Update status if out of stock
    if (product.getAvailableQuantity() <= 0) {
      product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
    }

    productRepository.save(product);

    // Update reservation
    reservation.setStatus(StockReservation.ReservationStatus.FULFILLED);
    reservation.setFulfilledAt(LocalDateTime.now());
    reservationRepository.save(reservation);

    // Create transaction
    createTransaction(product, InventoryTransaction.TransactionType.FULFILL,
        reservation.getQuantity(), product.getQuantity() + reservation.getQuantity(),
        product.getQuantity(), orderId, "ORDER", "Order fulfilled", "SYSTEM");

    log.info("Successfully fulfilled reservation for order ID: {}", orderId);
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> getProductsNeedingReorder() {
    log.info("Fetching products needing reorder");
    return productRepository.findProductsNeedingReorder().stream()
        .map(this::mapToProductResponse)
        .collect(Collectors.toList());
  }

  private void createTransaction(Product product, InventoryTransaction.TransactionType type,
      Integer quantity, Integer previousQuantity, Integer newQuantity,
      Long referenceId, String referenceType, String notes, String performedBy) {
    InventoryTransaction transaction = InventoryTransaction.builder()
        .product(product)
        .type(type)
        .quantity(quantity)
        .previousQuantity(previousQuantity)
        .newQuantity(newQuantity)
        .referenceId(referenceId)
        .referenceType(referenceType)
        .notes(notes)
        .performedBy(performedBy)
        .build();

    transactionRepository.save(transaction);
  }

  private ProductResponse mapToProductResponse(Product product) {
    return ProductResponse.builder()
        .id(product.getId())
        .sku(product.getSku())
        .name(product.getName())
        .description(product.getDescription())
        .category(product.getCategory())
        .price(product.getPrice())
        .quantity(product.getQuantity())
        .reservedQuantity(product.getReservedQuantity())
        .availableQuantity(product.getAvailableQuantity())
        .reorderLevel(product.getReorderLevel())
        .reorderQuantity(product.getReorderQuantity())
        .status(product.getStatus())
        .imageUrl(product.getImageUrl())
        .weight(product.getWeight())
        .weightUnit(product.getWeightUnit())
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .build();
  }
}