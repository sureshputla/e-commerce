# Common Module

Shared library containing Kafka event classes and topic constants. All services that participate in the Choreography Saga depend on this module.

## What's inside

```
common/
└── src/main/java/com/sureshputla/ecommerce/common/
    ├── event/
    │   ├── OrderCreatedEvent.java     ← published by order-service
    │   └── PaymentProcessedEvent.java ← published by payment-service
    └── kafka/
        └── KafkaTopics.java           ← topic name constants
```

## Event Classes

### `OrderCreatedEvent`
Published to topic `order.created` via the Outbox pattern.

| Field | Type | Description |
|-------|------|-------------|
| orderId | String | UUID of the order |
| userId | String | Customer identifier |
| paymentMethod | String | card / upi / cod |
| items | List<OrderItemData> | Snapshotted cart items |
| totalAmount | int | Total in INR |
| createdAt | LocalDateTime | Order creation time |

### `PaymentProcessedEvent`
Published to topic `payment.processed` via the Outbox pattern.

| Field | Type | Description |
|-------|------|-------------|
| paymentId | String | UUID of the payment |
| orderId | String | Associated order |
| userId | String | Customer identifier |
| amount | int | Amount charged in INR |
| success | boolean | true = SUCCESS, false = FAILED |
| message | String | Human-readable result or failure reason |
| processedAt | LocalDateTime | Payment processing time |

## Kafka Topics

| Constant | Value | Description |
|----------|-------|-------------|
| `KafkaTopics.ORDER_CREATED` | `order.created` | Order placed |
| `KafkaTopics.PAYMENT_PROCESSED` | `payment.processed` | Payment result |

## Dependencies
- `jackson-databind` – JSON serialisation of events
- `jackson-datatype-jsr310` – Java 8 date/time support (LocalDateTime)
- Lombok – `@Data @Builder @NoArgsConstructor @AllArgsConstructor`

## Used by
- **Produces** `OrderCreatedEvent`: `order-service`
- **Consumes** `OrderCreatedEvent`: `payment-service`, `cart-service`
- **Produces** `PaymentProcessedEvent`: `payment-service`
- **Consumes** `PaymentProcessedEvent`: `order-service`, `notification-service`

