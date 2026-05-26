package com.sureshputla.ecommerce.order.repository;

import com.sureshputla.ecommerce.order.model.Order;
import com.sureshputla.ecommerce.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Order> findByStatus(OrderStatus status);
}

