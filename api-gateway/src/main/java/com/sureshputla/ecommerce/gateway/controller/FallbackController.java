package com.sureshputla.ecommerce.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Provides circuit breaker fallback responses for all downstream services.
 * Returns user-friendly messages when a service is unreachable or the circuit is open.
 */
@RestController
public class FallbackController {

    @RequestMapping("/fallback/product")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> productFallback() {
        return Mono.just(Map.of("message", "Product service is temporarily unavailable. Please try again later."));
    }

    @RequestMapping("/fallback/cart")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> cartFallback() {
        return Mono.just(Map.of("message", "Cart service is temporarily unavailable. Please try again later."));
    }

    @RequestMapping("/fallback/wishlist")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> wishlistFallback() {
        return Mono.just(Map.of("message", "Wishlist service is temporarily unavailable. Please try again later."));
    }

    @RequestMapping("/fallback/order")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> orderFallback() {
        return Mono.just(Map.of("message", "Order service is temporarily unavailable. Please try again later."));
    }

    @RequestMapping("/fallback/payment")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> paymentFallback() {
        return Mono.just(Map.of("message", "Payment service is temporarily unavailable. Please try again later."));
    }

    @RequestMapping("/fallback/notification")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> notificationFallback() {
        return Mono.just(Map.of("message", "Notification service is temporarily unavailable. Please try again later."));
    }
}

