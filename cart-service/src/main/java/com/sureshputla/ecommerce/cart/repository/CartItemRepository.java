package com.sureshputla.ecommerce.cart.repository;

import com.sureshputla.ecommerce.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserIdOrderByProductIdAsc(String userId);

    Optional<CartItem> findByUserIdAndProductId(String userId, Integer productId);

    void deleteByUserId(String userId);
}

