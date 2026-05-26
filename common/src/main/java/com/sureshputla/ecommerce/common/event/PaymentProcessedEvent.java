package com.sureshputla.ecommerce.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published by Payment Service after processing a payment.
 * Consumed by: OrderService (to update order status), NotificationService (to send notifications).
 * Part of the Choreography-based Saga pattern.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {

    private String paymentId;
    private String orderId;
    private String userId;
    private int amount;
    private boolean success;
    private String message;
    private LocalDateTime processedAt;
}

