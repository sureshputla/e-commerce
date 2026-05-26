package com.sureshputla.ecommerce.wishlist.service;

import com.sureshputla.ecommerce.wishlist.client.ProductServiceClient;
import com.sureshputla.ecommerce.wishlist.dto.ProductDto;
import com.sureshputla.ecommerce.wishlist.dto.WishlistItemDto;
import com.sureshputla.ecommerce.wishlist.model.WishlistItem;
import com.sureshputla.ecommerce.wishlist.repository.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {

    private static final String DEFAULT_USER_ID = "user1";

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductServiceClient productServiceClient;

    @Transactional(readOnly = true)
    public List<WishlistItemDto> getWishlist() {
        return wishlistItemRepository.findByUserIdOrderByProductIdAsc(DEFAULT_USER_ID)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public List<WishlistItemDto> addToWishlist(Integer productId) {
        // Idempotent – skip if already present
        if (wishlistItemRepository.findByUserIdAndProductId(DEFAULT_USER_ID, productId).isPresent()) {
            log.debug("Product {} already in wishlist for {}", productId, DEFAULT_USER_ID);
            return getWishlist();
        }

        ResponseEntity<ProductDto> response = productServiceClient.getProduct(productId);
        ProductDto product = response.getBody();

        if (product == null || product.getPrice() == 0) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        WishlistItem item = WishlistItem.builder()
                .userId(DEFAULT_USER_ID)
                .productId(product.getId())
                .productName(product.getName())
                .category(product.getCategory())
                .brand(product.getBrand())
                .price(product.getPrice())
                .build();

        wishlistItemRepository.save(item);
        log.info("Added product {} to wishlist for {}", productId, DEFAULT_USER_ID);
        return getWishlist();
    }

    @Transactional
    public List<WishlistItemDto> removeFromWishlist(Integer productId) {
        wishlistItemRepository.deleteByUserIdAndProductId(DEFAULT_USER_ID, productId);
        log.info("Removed product {} from wishlist for {}", productId, DEFAULT_USER_ID);
        return getWishlist();
    }

    private WishlistItemDto toDto(WishlistItem item) {
        return WishlistItemDto.builder()
                .id(item.getProductId())
                .name(item.getProductName())
                .category(item.getCategory())
                .brand(item.getBrand())
                .price(item.getPrice())
                .build();
    }
}

