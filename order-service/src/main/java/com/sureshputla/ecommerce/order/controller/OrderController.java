package com.sureshputla.ecommerce.order.controller;

import com.sureshputla.ecommerce.order.dto.OrderResponse;
import com.sureshputla.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order lifecycle and checkout")
public class OrderController {

    private final OrderService orderService;

    /**
     * Checkout endpoint – compatible with the original monolith API.
     * Returns {@code {"message": "..."}} on success.
     */
    @PostMapping("/api/checkout")
    @Operation(summary = "Place an order (checkout) – initiates the payment Saga")
    public OrderResponse checkout(@RequestParam(defaultValue = "card") String paymentMethod) {
        return orderService.checkout(paymentMethod);
    }

    @GetMapping("/api/orders")
    @Operation(summary = "List all orders for the current user")
    public List<OrderResponse> getOrders() {
        return orderService.getOrdersForUser();
    }

    @GetMapping("/api/orders/{orderId}")
    @Operation(summary = "Get a specific order by ID")
    public OrderResponse getOrder(@PathVariable String orderId) {
        return orderService.getOrder(orderId);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(RuntimeException ex) {
        return Map.of("message", ex.getMessage());
    }
}

