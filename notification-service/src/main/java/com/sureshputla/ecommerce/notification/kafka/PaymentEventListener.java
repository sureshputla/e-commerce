package com.sureshputla.ecommerce.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sureshputla.ecommerce.common.event.PaymentProcessedEvent;
import com.sureshputla.ecommerce.common.kafka.KafkaTopics;
import com.sureshputla.ecommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to {@code payment.processed} and persists notification records.
 * This is the final step in the Choreography Saga.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.PAYMENT_PROCESSED, groupId = "${spring.application.name}")
    public void onPaymentProcessed(String payload) {
        try {
            PaymentProcessedEvent event = objectMapper.readValue(payload, PaymentProcessedEvent.class);
            log.info("PaymentProcessedEvent received – paymentId={} orderId={} success={}",
                    event.getPaymentId(), event.getOrderId(), event.isSuccess());
            notificationService.handlePaymentProcessed(event);
        } catch (Exception e) {
            log.error("Failed to process PaymentProcessedEvent: {}", payload, e);
        }
    }
}

