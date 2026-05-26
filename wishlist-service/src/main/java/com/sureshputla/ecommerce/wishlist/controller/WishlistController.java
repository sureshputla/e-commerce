package com.sureshputla.ecommerce.wishlist.controller;

import com.sureshputla.ecommerce.wishlist.dto.WishlistItemDto;
import com.sureshputla.ecommerce.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Save products for later")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Get wishlist items")
    public List<WishlistItemDto> getWishlist() {
        return wishlistService.getWishlist();
    }

    @PostMapping("/{productId}")
    @Operation(summary = "Add a product to the wishlist")
    public List<WishlistItemDto> addToWishlist(@PathVariable Integer productId) {
        return wishlistService.addToWishlist(productId);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove a product from the wishlist")
    public List<WishlistItemDto> removeFromWishlist(@PathVariable Integer productId) {
        return wishlistService.removeFromWishlist(productId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(IllegalArgumentException ex) {
        return Map.of("message", ex.getMessage());
    }
}

