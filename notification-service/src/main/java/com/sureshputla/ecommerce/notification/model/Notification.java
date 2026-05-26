package com.sureshputla.ecommerce.notification.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String orderId;

    private String paymentId;

    @Column(nullable = false)
    private String type; // "PAYMENT_SUCCESS" | "PAYMENT_FAILED"

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime createdAt;
}

