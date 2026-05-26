package com.sureshputla.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private Integer productId;
    private String productName;
    private int quantity;
    private int unitPrice;
    private int subtotal;
}

