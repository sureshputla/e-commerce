package com.sureshputla.ecommerce.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sureshputla.ecommerce.common.event.OrderCreatedEvent;
import com.sureshputla.ecommerce.common.kafka.KafkaTopics;
import com.sureshputla.ecommerce.order.client.CartServiceClient;
import com.sureshputla.ecommerce.order.dto.CartResponse;
import com.sureshputla.ecommerce.order.dto.OrderItemDto;
import com.sureshputla.ecommerce.order.dto.OrderResponse;
import com.sureshputla.ecommerce.order.model.Order;
import com.sureshputla.ecommerce.order.model.OrderItem;
import com.sureshputla.ecommerce.order.model.OrderStatus;
import com.sureshputla.ecommerce.order.model.OutboxEvent;
import com.sureshputla.ecommerce.order.repository.OrderRepository;
import com.sureshputla.ecommerce.order.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final String DEFAULT_USER_ID = "user1";

    private static final java.util.Map<String, String> PAYMENT_LABELS = java.util.Map.of(
            "card", "Card",
            "upi", "UPI",
            "cod", "Cash on Delivery"
    );

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final CartServiceClient cartServiceClient;
    private final ObjectMapper objectMapper;

    /**
     * Checkout flow – Saga initiator.
     * <ol>
     *   <li>Fetch cart items from cart-service via Feign.</li>
     *   <li>Create Order entity (PENDING).</li>
     *   <li>Persist {@link OutboxEvent} in the <em>same DB transaction</em> (Outbox Pattern).</li>
     *   <li>Return immediately – the {@code OutboxPublisher} will relay the event to Kafka async.</li>
     * </ol>
     */
    @Transactional
    public OrderResponse checkout(String paymentMethod) {
        String normalizedMethod = paymentMethod == null ? "card"
                : paymentMethod.toLowerCase(java.util.Locale.ROOT).trim();

        if (!PAYMENT_LABELS.containsKey(normalizedMethod)) {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }

        CartResponse cart = cartServiceClient.getCart();
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty. Add products before checkout.");
        }

        // Build Order
        String orderId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        Order order = Order.builder()
                .id(orderId)
                .userId(DEFAULT_USER_ID)
                .status(OrderStatus.PENDING)
                .paymentMethod(normalizedMethod)
                .totalAmount(cart.getTotal())
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Build OrderItems (snapshotting cart)
        List<OrderItem> items = cart.getItems().stream().map(cartItem ->
                OrderItem.builder()
                        .order(order)
                        .productId(cartItem.getProduct().getId())
                        .productName(cartItem.getProduct().getName())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getProduct().getPrice())
                        .subtotal(cartItem.getSubtotal())
                        .build()
        ).toList();
        order.getItems().addAll(items);

        orderRepository.save(order);
        log.info("Created order id={} for userId={} totalAmount={}", orderId, DEFAULT_USER_ID, cart.getTotal());

        // Outbox Pattern – store event atomically with the order
        OrderCreatedEvent event = buildOrderCreatedEvent(order);
        saveOutboxEvent(orderId, event);

        return toResponse(order,
                "Order placed successfully! Payment processing via " + PAYMENT_LABELS.get(normalizedMethod) + "...");
    }

    @Transactional
    public void confirmOrder(String orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(OrderStatus.CONFIRMED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            log.info("Order {} CONFIRMED", orderId);
        });
    }

    @Transactional
    public void cancelOrder(String orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            log.info("Order {} CANCELLED (payment failed)", orderId);
        });
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .map(o -> toResponse(o, null))
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForUser() {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(DEFAULT_USER_ID)
                .stream()
                .map(o -> toResponse(o, null))
                .toList();
    }

    // ── private helpers ─────────────────────────────────────────────────────

    private void saveOutboxEvent(String orderId, OrderCreatedEvent event) {
        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("Order")
                    .aggregateId(orderId)
                    .eventType(KafkaTopics.ORDER_CREATED)
                    .payload(objectMapper.writeValueAsString(event))
                    .status(OutboxEvent.OutboxStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
            outboxEventRepository.save(outboxEvent);
            log.debug("Saved OutboxEvent for order={}", orderId);
        } catch (Exception e) {
            log.error("Failed to serialise OutboxEvent for order={}", orderId, e);
            throw new RuntimeException("Failed to create outbox event", e);
        }
    }

    private OrderCreatedEvent buildOrderCreatedEvent(Order order) {
        List<OrderCreatedEvent.OrderItemData> itemData = order.getItems().stream()
                .map(i -> OrderCreatedEvent.OrderItemData.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build())
                .toList();

        return OrderCreatedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .paymentMethod(order.getPaymentMethod())
                .items(itemData)
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderResponse toResponse(Order order, String message) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(i -> OrderItemDto.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .subtotal(i.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .totalAmount(order.getTotalAmount())
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .message(message)
                .build();
    }
}

