# Order Service

Manages order lifecycle. Acts as the **Saga Initiator** and uses the **Transactional Outbox Pattern** for reliable event publishing.

## Port
`8084`

## Technologies
- Spring Boot 3.3.5 (Web + JPA)
- H2 In-Memory Database
- Lombok, MapStruct
- Spring Cloud OpenFeign (calls cart-service)
- Resilience4j (Feign fallback)
- Spring Kafka (producer via Outbox + consumer)
- Spring Cloud Eureka Client
- Springdoc OpenAPI

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST   | `/api/checkout?paymentMethod={card\|upi\|cod}` | Place order (starts Saga) |
| GET    | `/api/orders` | List all orders for the current user |
| GET    | `/api/orders/{orderId}` | Get order details and status |

## Saga – Choreography

```
POST /api/checkout
    │
    ▼
OrderService.checkout()
  1. Calls cart-service (Feign) to get cart items
  2. Creates Order entity (status=PENDING) + OrderItems
  3. Saves OutboxEvent(OrderCreatedEvent) in SAME transaction
  ▼
OutboxPublisher (@Scheduled every 5s)
  Reads PENDING events → sends to Kafka "order.created" → marks PUBLISHED
```

## Outbox Pattern – outbox_events table

| Column        | Description |
|---------------|-------------|
| id            | UUID |
| aggregate_type | "Order" |
| aggregate_id  | orderId |
| event_type    | Kafka topic ("order.created") |
| payload       | JSON-serialised OrderCreatedEvent |
| status        | PENDING → PUBLISHED (or FAILED) |
| created_at    | Timestamp |
| published_at  | Timestamp set after Kafka ACK |

## Kafka Consumers

| Topic | Action |
|-------|--------|
| `payment.processed` | Updates order status to CONFIRMED (success) or CANCELLED (failure) |

## Order Status Flow

```
PENDING → CONFIRMED  (payment succeeded)
        → CANCELLED  (payment failed – compensating transaction)
```

## Payment Failure Simulation (Card only)
If the order total ends in digit `3`, the simulated card payment fails → order is CANCELLED.

## Run
```bash
# Prerequisites: service-registry, cart-service, Kafka
cd order-service
mvn spring-boot:run
```

## Useful URLs
| URL | Description |
|-----|-------------|
| http://localhost:8084/swagger-ui.html | Swagger UI |
| http://localhost:8084/h2-console | H2 Console (`jdbc:h2:mem:orderdb`) |

