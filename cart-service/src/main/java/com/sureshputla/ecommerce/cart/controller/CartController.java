package com.sureshputla.ecommerce.cart.controller;

import com.sureshputla.ecommerce.cart.dto.CartResponse;
import com.sureshputla.ecommerce.cart.service.CartService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current cart")
    public CartResponse getCart() {
        return cartService.getCart();
    }

    @PostMapping("/{productId}")
    @Operation(summary = "Add a product to the cart")
    public CartResponse addToCart(@PathVariable Integer productId) {
        return cartService.addToCart(productId);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove a product from the cart")
    public CartResponse removeFromCart(@PathVariable Integer productId) {
        return cartService.removeFromCart(productId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(IllegalArgumentException ex) {
        return Map.of("message", ex.getMessage());
    }
}

