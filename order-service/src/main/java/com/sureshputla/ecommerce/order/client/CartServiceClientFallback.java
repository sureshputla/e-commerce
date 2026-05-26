package com.sureshputla.ecommerce.order.client;

import com.sureshputla.ecommerce.order.dto.CartResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Slf4j
public class CartServiceClientFallback implements CartServiceClient {

    @Override
    public CartResponse getCart() {
        log.warn("cart-service circuit open – returning empty cart fallback");
        return CartResponse.builder()
                .items(Collections.emptyList())
                .total(0)
                .build();
    }
}

