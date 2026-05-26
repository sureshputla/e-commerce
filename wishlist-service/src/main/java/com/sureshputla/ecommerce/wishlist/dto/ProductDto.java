package com.sureshputla.ecommerce.wishlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Local copy of product fields. Each service owns its DTO to avoid coupling. */
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

