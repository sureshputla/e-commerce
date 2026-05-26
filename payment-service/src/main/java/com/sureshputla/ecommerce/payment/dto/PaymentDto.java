package com.sureshputla.ecommerce.payment.dto;

import com.sureshputla.ecommerce.payment.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private String paymentId;
    private String orderId;
    private String userId;
    private Integer amount;
    private String paymentMethod;
    private PaymentStatus status;
    private String failureReason;
    private LocalDateTime processedAt;
}

