package com.sureshputla.ecommerce.wishlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wishlist item response – matches the original monolith API returning product-like objects.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemDto {
    private Integer id;
    private String name;
    private String category;
    private String brand;
    private Integer price;
}

