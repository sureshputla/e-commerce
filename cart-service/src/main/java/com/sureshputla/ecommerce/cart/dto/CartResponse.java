package com.sureshputla.ecommerce.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Full cart response. Matches the original monolith API contract:
 * { "items": [...], "total": 799 }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private List<CartItemDto> items;
    private int total;
}

