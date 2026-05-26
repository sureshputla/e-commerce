package com.sureshputla.ecommerce.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Transactional Outbox Pattern for Payment Service.
 * Stores PaymentProcessedEvent before it is published to Kafka.
 */
@Entity
@Table(name = "outbox_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    private String id;

    private String aggregateType;
    private String aggregateId;

    /** Kafka topic name. */
    private String eventType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.PENDING;

    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;

    public enum OutboxStatus {
        PENDING, PUBLISHED, FAILED
    }
}

