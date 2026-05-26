# Payment Service

Processes payments as a **Saga participant**. Uses the **Transactional Outbox Pattern** to reliably publish `PaymentProcessedEvent`.

## Port
`8085`

## Technologies
- Spring Boot 3.3.5 (Web + JPA)
- H2 In-Memory Database
- Lombok
- Spring Kafka (consumer + producer via Outbox)
- Spring Cloud Eureka Client
- Springdoc OpenAPI

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET    | `/api/payments/{paymentId}` | Get payment by payment ID |
| GET    | `/api/payments/order/{orderId}` | Get payment by order ID |

## Saga Role – Participant

```
Kafka "order.created"
    │
    ▼
OrderEventListener.onOrderCreated()
    │
    ▼
PaymentService.processPayment(OrderCreatedEvent)
  1. Create Payment entity (SUCCESS or FAILED)
  2. Save OutboxEvent(PaymentProcessedEvent) in SAME transaction
    │
    ▼
OutboxPublisher (@Scheduled every 5s)
  Publishes to Kafka "payment.processed"
```

## Payment Simulation

| Method | Behaviour |
|--------|-----------|
| `cod`  | Always succeeds |
| `upi`  | Always succeeds |
| `card` | Fails if `totalAmount % 10 == 3` (test hook) |

## Outbox Pattern
Same outbox mechanism as order-service. `outbox_events` table stores PENDING events which are polled and published every 5 s.

## Idempotency
If a `PaymentProcessedEvent` for an `orderId` already exists in the DB, the consumer skips reprocessing. This handles Kafka's at-least-once delivery.

## Run
```bash
# Prerequisites: service-registry, Kafka
cd payment-service
mvn spring-boot:run
```

## Useful URLs
| URL | Description |
|-----|-------------|
| http://localhost:8085/swagger-ui.html | Swagger UI |
| http://localhost:8085/h2-console | H2 Console (`jdbc:h2:mem:paymentdb`) |

