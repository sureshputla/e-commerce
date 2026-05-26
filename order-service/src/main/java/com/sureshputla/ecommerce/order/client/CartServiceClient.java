package com.sureshputla.ecommerce.order.client;

import com.sureshputla.ecommerce.order.dto.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Feign client for cart-service.
 * Called during checkout to snapshot cart items into the order.
 */
@FeignClient(name = "cart-service", fallback = CartServiceClientFallback.class)
public interface CartServiceClient {

    @GetMapping("/api/cart")
    CartResponse getCart();
}

