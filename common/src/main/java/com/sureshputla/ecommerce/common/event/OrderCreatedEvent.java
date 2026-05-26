package com.sureshputla.ecommerce.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event published by Order Service when a new order is created.
 * Consumed by: PaymentService (to process payment), CartService (to clear cart).
 * Part of the Choreography-based Saga pattern.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private String orderId;
    private String userId;
    private String paymentMethod;
    private List<OrderItemData> items;
    private int totalAmount;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemData {
        private int productId;
        private String productName;
        private int quantity;
        private int unitPrice;
    }
}

