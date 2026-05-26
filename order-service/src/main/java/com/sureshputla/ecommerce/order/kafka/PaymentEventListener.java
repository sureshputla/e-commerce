package com.sureshputla.ecommerce.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sureshputla.ecommerce.common.event.PaymentProcessedEvent;
import com.sureshputla.ecommerce.common.kafka.KafkaTopics;
import com.sureshputla.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Saga participant – listens to {@code payment.processed} events.
 * <ul>
 *   <li>Payment succeeded → CONFIRM the order (happy path).</li>
 *   <li>Payment failed → CANCEL the order (compensating transaction).</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.PAYMENT_PROCESSED, groupId = "${spring.application.name}")
    public void onPaymentProcessed(String payload) {
        try {
            PaymentProcessedEvent event = objectMapper.readValue(payload, PaymentProcessedEvent.class);
            log.info("PaymentProcessedEvent received – orderId={}, success={}",
                    event.getOrderId(), event.isSuccess());

            if (event.isSuccess()) {
                orderService.confirmOrder(event.getOrderId());
            } else {
                orderService.cancelOrder(event.getOrderId());
            }
        } catch (Exception e) {
            log.error("Failed to process PaymentProcessedEvent: {}", payload, e);
        }
    }
}

