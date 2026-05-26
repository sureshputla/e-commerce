package com.sureshputla.ecommerce.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single item in the cart response.
 * Maintains backward compatibility with the original monolith API shape:
 * { "product": {...}, "quantity": 1, "subtotal": 799 }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {

    private ProductDto product;
    private int quantity;
    private int subtotal;
}

