package com.sureshputla.ecommerce.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sureshputla.ecommerce.common.event.OrderCreatedEvent;
import com.sureshputla.ecommerce.common.event.PaymentProcessedEvent;
import com.sureshputla.ecommerce.common.kafka.KafkaTopics;
import com.sureshputla.ecommerce.payment.dto.PaymentDto;
import com.sureshputla.ecommerce.payment.model.OutboxEvent;
import com.sureshputla.ecommerce.payment.model.Payment;
import com.sureshputla.ecommerce.payment.model.PaymentStatus;
import com.sureshputla.ecommerce.payment.repository.OutboxEventRepository;
import com.sureshputla.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Processes payment for an order.
     * <p>
     * Saga participant – triggered by {@code order.created} Kafka event.
     * Persists a {@link Payment} record and an {@link OutboxEvent} (PaymentProcessedEvent)
     * in the <em>same DB transaction</em> (Transactional Outbox Pattern).
     * <p>
     * The payment simulation succeeds for COD/UPI always; for CARD it succeeds
     * unless the total amount ends in 3 (test scenario for failure).
     */
    @Transactional
    public void processPayment(OrderCreatedEvent orderEvent) {
        // Idempotency – skip if already processed
        if (paymentRepository.findByOrderId(orderEvent.getOrderId()).isPresent()) {
            log.warn("Payment already processed for orderId={}", orderEvent.getOrderId());
            return;
        }

        String paymentId = UUID.randomUUID().toString();
        boolean success = simulatePayment(orderEvent.getPaymentMethod(), orderEvent.getTotalAmount());
        String failureReason = success ? null : "Insufficient funds (simulated)";
        PaymentStatus status = success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        LocalDateTime now = LocalDateTime.now();

        Payment payment = Payment.builder()
                .id(paymentId)
                .orderId(orderEvent.getOrderId())
                .userId(orderEvent.getUserId())
                .amount(orderEvent.getTotalAmount())
                .paymentMethod(orderEvent.getPaymentMethod())
                .status(status)
                .failureReason(failureReason)
                .processedAt(now)
                .build();
        paymentRepository.save(payment);

        // Outbox – store event atomically
        PaymentProcessedEvent processedEvent = PaymentProcessedEvent.builder()
                .paymentId(paymentId)
                .orderId(orderEvent.getOrderId())
                .userId(orderEvent.getUserId())
                .amount(orderEvent.getTotalAmount())
                .success(success)
                .message(success
                        ? "Payment successful via " + orderEvent.getPaymentMethod() + " for ₹" + orderEvent.getTotalAmount()
                        : failureReason)
                .processedAt(now)
                .build();

        saveOutboxEvent(paymentId, processedEvent);
        log.info("Payment processed – paymentId={} orderId={} success={}", paymentId, orderEvent.getOrderId(), success);
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentByOrder(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderId));
    }

    @Transactional(readOnly = true)
    public PaymentDto getPayment(String paymentId) {
        return paymentRepository.findById(paymentId)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
    }

    // ── private helpers ─────────────────────────────────────────────────────

    /**
     * Simulates payment:
     * – COD always succeeds.
     * – UPI always succeeds.
     * – Card fails when totalAmount ends in digit 3 (test hook).
     */
    private boolean simulatePayment(String method, int amount) {
        if ("cod".equalsIgnoreCase(method) || "upi".equalsIgnoreCase(method)) {
            return true;
        }
        // Card: fail if amount mod 10 == 3 (test scenario)
        return (amount % 10) != 3;
    }

    private void saveOutboxEvent(String paymentId, PaymentProcessedEvent event) {
        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("Payment")
                    .aggregateId(paymentId)
                    .eventType(KafkaTopics.PAYMENT_PROCESSED)
                    .payload(objectMapper.writeValueAsString(event))
                    .status(OutboxEvent.OutboxStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            log.error("Failed to save payment outbox event for paymentId={}", paymentId, e);
            throw new RuntimeException("Outbox serialization failed", e);
        }
    }

    private PaymentDto toDto(Payment p) {
        return PaymentDto.builder()
                .paymentId(p.getId())
                .orderId(p.getOrderId())
                .userId(p.getUserId())
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod())
                .status(p.getStatus())
                .failureReason(p.getFailureReason())
                .processedAt(p.getProcessedAt())
                .build();
    }
}

