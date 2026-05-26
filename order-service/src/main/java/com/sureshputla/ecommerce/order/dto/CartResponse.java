package com.sureshputla.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Cart response shape returned by cart-service.
 * Kept as a local DTO to decouple the two services.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private List<CartItemDto> items;
    private int total;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemDto {
        private ProductDto product;
        private int quantity;
        private int subtotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDto {
        private int id;
        private String name;
        private String category;
        private String brand;
        private int price;
    }
}

