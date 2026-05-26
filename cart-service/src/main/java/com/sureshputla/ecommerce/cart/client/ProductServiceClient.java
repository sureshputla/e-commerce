package com.sureshputla.ecommerce.cart.client;

import com.sureshputla.ecommerce.cart.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for the product-service.
 * Uses client-side load balancing via Eureka (lb://product-service).
 * Falls back to {@link ProductServiceClientFallback} when the circuit is open.
 */
@FeignClient(name = "product-service", fallback = ProductServiceClientFallback.class)
public interface ProductServiceClient {

    @GetMapping("/api/products/{id}")
    ResponseEntity<ProductDto> getProduct(@PathVariable Integer id);
}

