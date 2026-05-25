package com.sureshputla.ecommerce.controller;

import com.sureshputla.ecommerce.model.Product;
import com.sureshputla.ecommerce.service.ProductCatalogService;
import com.sureshputla.ecommerce.service.StoreService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StoreApiController {
    private final ProductCatalogService productCatalogService;
    private final StoreService storeService;

    public StoreApiController(ProductCatalogService productCatalogService, StoreService storeService) {
        this.productCatalogService = productCatalogService;
        this.storeService = storeService;
    }

    @GetMapping("/filters")
    public FilterResponse getFilters() {
        return new FilterResponse(productCatalogService.getCategories(), productCatalogService.getBrands());
    }

    @GetMapping("/products")
    public List<Product> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String search
    ) {
        return productCatalogService.getProducts(category, brand, maxPrice, search);
    }

    @GetMapping("/wishlist")
    public List<Product> getWishlist() {
        return storeService.getWishlistProducts();
    }

    @PostMapping("/wishlist/{productId}")
    public List<Product> addToWishlist(@PathVariable int productId) {
        return storeService.addToWishlist(productId);
    }

    @DeleteMapping("/wishlist/{productId}")
    public List<Product> removeFromWishlist(@PathVariable int productId) {
        return storeService.removeFromWishlist(productId);
    }

    @GetMapping("/cart")
    public CartResponse getCart() {
        return toCartResponse(storeService.getCartState());
    }

    @PostMapping("/cart/{productId}")
    public CartResponse addToCart(@PathVariable int productId) {
        return toCartResponse(storeService.addToCart(productId));
    }

    @DeleteMapping("/cart/{productId}")
    public CartResponse removeFromCart(@PathVariable int productId) {
        return toCartResponse(storeService.removeFromCart(productId));
    }

    @PostMapping("/checkout")
    public PaymentResponse checkout(@RequestParam String paymentMethod) {
        StoreService.PaymentState paymentState = storeService.checkout(paymentMethod);
        return new PaymentResponse(paymentState.message());
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(RuntimeException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    private CartResponse toCartResponse(StoreService.CartState state) {
        return new CartResponse(state.items(), state.total());
    }

    public record FilterResponse(List<String> categories, List<String> brands) {
    }

    public record CartResponse(List<com.sureshputla.ecommerce.model.CartItem> items, int total) {
    }

    public record PaymentResponse(String message) {
    }

    public record ErrorResponse(String message) {
    }
}
