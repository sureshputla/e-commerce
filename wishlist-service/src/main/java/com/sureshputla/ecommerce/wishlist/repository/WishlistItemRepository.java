package com.sureshputla.ecommerce.wishlist.repository;

import com.sureshputla.ecommerce.wishlist.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByUserIdOrderByProductIdAsc(String userId);

    Optional<WishlistItem> findByUserIdAndProductId(String userId, Integer productId);

    void deleteByUserIdAndProductId(String userId, Integer productId);
}

