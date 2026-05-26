package com.sureshputla.ecommerce.order.model;

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
 * Transactional Outbox Pattern – stores outgoing domain events alongside business
 * data in the same DB transaction, guaranteeing at-least-once delivery to Kafka.
 *
 * <pre>
 * Write path:  OrderService saves Order + OutboxEvent atomically (same @Transactional)
 * Read path:   OutboxPublisher @Scheduled reads PENDING rows, publishes, marks PUBLISHED
 * </pre>
 */
@Entity
@Table(name = "outbox_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    private String id;           // UUID

    private String aggregateType;  // "Order"
    private String aggregateId;    // orderId

    /** Kafka topic name (see {@link com.sureshputla.ecommerce.common.kafka.KafkaTopics}). */
    private String eventType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;       // JSON-serialised event

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.PENDING;

    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;

    public enum OutboxStatus {
        PENDING, PUBLISHED, FAILED
    }
}

