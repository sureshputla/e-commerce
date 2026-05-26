package com.sureshputla.ecommerce.payment.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sureshputla.ecommerce.common.event.OrderCreatedEvent;
import com.sureshputla.ecommerce.common.kafka.KafkaTopics;
import com.sureshputla.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Saga participant – listens for {@code order.created} events and processes payment.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "${spring.application.name}")
    public void onOrderCreated(String payload) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            log.info("OrderCreatedEvent received – orderId={} amount={}", event.getOrderId(), event.getTotalAmount());
            paymentService.processPayment(event);
        } catch (Exception e) {
            log.error("Failed to process OrderCreatedEvent payload: {}", payload, e);
        }
    }
}

