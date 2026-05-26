package com.sureshputla.ecommerce.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Local ProductDto used for Feign client responses from product-service.
 * Each service owns its own DTO copy to avoid tight coupling.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Integer id;
    private String name;
    private String category;
    private String brand;
    private Integer price;
    private Integer stockQuantity;
}

