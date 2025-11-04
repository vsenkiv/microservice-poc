package com.microservices.order.repository;

import com.microservices.order.entity.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  List<Order> findByUserId(Long userId);

  // Add this to OrderRepository interface
  List<Order> findByUserIdOrderByIdDesc(Long userId);
}