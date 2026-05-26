package com.sureshputla.ecommerce.wishlist.client;

import com.sureshputla.ecommerce.wishlist.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", fallback = ProductServiceClientFallback.class)
public interface ProductServiceClient {

    @GetMapping("/api/products/{id}")
    ResponseEntity<ProductDto> getProduct(@PathVariable Integer id);
}

