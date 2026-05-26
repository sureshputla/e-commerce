package com.sureshputla.ecommerce.cart.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sureshputla.ecommerce.cart.service.CartService;
import com.sureshputla.ecommerce.common.event.OrderCreatedEvent;
import com.sureshputla.ecommerce.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to the {@code order.created} topic.
 * When an order is placed, this listener clears the user's cart
 * as the compensating/continuation step in the Saga choreography.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final CartService cartService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "${spring.application.name}")
    public void onOrderCreated(String payload) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            log.info("Received OrderCreatedEvent – orderId={}, userId={}. Clearing cart...",
                    event.getOrderId(), event.getUserId());
            cartService.clearCart(event.getUserId());
        } catch (Exception e) {
            log.error("Failed to process OrderCreatedEvent payload: {}", payload, e);
        }
    }
}

