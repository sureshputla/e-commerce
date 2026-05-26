package com.sureshputla.ecommerce.order.dto;

import com.sureshputla.ecommerce.order.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private String userId;
    private OrderStatus status;
    private String paymentMethod;
    private int totalAmount;
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Convenience field used on checkout response (matches original monolith API). */
    private String message;
}

