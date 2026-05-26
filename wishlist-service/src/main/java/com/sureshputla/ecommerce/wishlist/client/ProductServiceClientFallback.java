package com.sureshputla.ecommerce.wishlist.client;

import com.sureshputla.ecommerce.wishlist.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductServiceClientFallback implements ProductServiceClient {

    @Override
    public ResponseEntity<ProductDto> getProduct(Integer id) {
        log.warn("product-service circuit open – returning fallback for productId={}", id);
        return ResponseEntity.ok(ProductDto.builder()
                .id(id)
                .name("Product Unavailable")
                .category("Unknown")
                .brand("Unknown")
                .price(0)
                .build());
    }
}

