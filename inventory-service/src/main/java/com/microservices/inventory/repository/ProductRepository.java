package com.microservices.inventory.repository;

import com.microservices.inventory.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  Optional<Product> findBySku(String sku);

  List<Product> findByCategory(String category);

  List<Product> findByStatus(Product.ProductStatus status);

  @Query("SELECT p FROM Product p WHERE p.quantity - p.reservedQuantity >= :quantity AND p.status = 'ACTIVE'")
  List<Product> findAvailableProducts(@Param("quantity") Integer quantity);

  @Query("SELECT p FROM Product p WHERE p.quantity - p.reservedQuantity <= p.reorderLevel")
  List<Product> findProductsNeedingReorder();

  @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword% OR p.sku LIKE %:keyword%")
  List<Product> searchProducts(@Param("keyword") String keyword);

  @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.quantity - p.reservedQuantity > 0")
  List<Product> findActiveProductsInStock();

  boolean existsBySku(String sku);
}