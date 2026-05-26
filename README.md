# 🛍️ E-Commerce Microservices Platform

A fully decomposed e-commerce backend built with **Spring Boot 3**, **Spring Cloud**, **Kafka**, and enterprise patterns.

---

## 📐 Architecture Overview

```
Browser (port 8090)
    │
    ▼
Frontend Service
    │  HTTP (cross-origin, CORS enabled on gateway)
    ▼
API Gateway (port 8080)  ← Resilience4j Circuit Breakers + Eureka Load Balancing
    │
    ├──► product-service  (8081) – catalog
    ├──► cart-service     (8082) – cart per user
    ├──► wishlist-service (8083) – wishlist per user
    ├──► order-service    (8084) – orders + Saga initiator + Outbox
    ├──► payment-service  (8085) – payments + Outbox
    └──► notification-service (8086) – Kafka consumer

  Sync (Feign):
    cart-service     → product-service
    wishlist-service → product-service
    order-service    → cart-service

  Async (Kafka):
    order-service      publishes "order.created"
      ├── cart-service        consumes → clears cart
      └── payment-service     consumes → processes payment → publishes "payment.processed"
            ├── order-service       consumes → CONFIRMS or CANCELS order
            └── notification-service consumes → saves notification

Eureka Service Registry (port 8761) – all services register here
Kafka UI (port 8989)                – inspect topics & messages
```

---

## 🏗️ Design Patterns

### Choreography-based Saga
Each service reacts to events autonomously — no central orchestrator.

```
1. POST /api/checkout  →  OrderService creates Order (PENDING)
2. Outbox Publisher    →  Publishes OrderCreatedEvent to Kafka "order.created"
3. PaymentService      →  Listens, processes payment, publishes PaymentProcessedEvent
4. CartService         →  Listens to "order.created", clears cart (compensating action)
5. OrderService        →  Listens to "payment.processed" → CONFIRMED or CANCELLED
6. NotificationService →  Listens to "payment.processed" → saves notification record
```

### Transactional Outbox Pattern
Used in `order-service` and `payment-service`:
- Domain event saved to `outbox_events` table **in the same DB transaction** as the business entity.
- `@Scheduled` `OutboxPublisher` polls every 5 s, publishes to Kafka, marks as `PUBLISHED`.
- Guarantees **at-least-once delivery** even if Kafka is temporarily unavailable.

### Circuit Breaker (Resilience4j)
- Every API Gateway route has a dedicated circuit breaker with a friendly fallback.
- Feign clients also have fallbacks for graceful degradation.

---

## 📦 Technology Stack

| Concern              | Technology |
|----------------------|------------|
| Framework            | Spring Boot 3.3.5 |
| Service Discovery    | Spring Cloud Netflix Eureka |
| API Gateway          | Spring Cloud Gateway (WebFlux) |
| Service Comm (sync)  | Spring Cloud OpenFeign + LoadBalancer |
| Circuit Breaker      | Resilience4j |
| Messaging (async)    | Apache Kafka |
| Persistence          | Spring Data JPA + H2 (dev) |
| Code Generation      | Lombok, MapStruct |
| API Docs             | Springdoc OpenAPI (Swagger UI) |
| Build Tool           | Apache Maven (multi-module) |
| Containerisation     | Docker Compose (Kafka stack) |

---

## 🚀 Startup Sequence

> **Prerequisites:** Java 17+, Maven 3.8+, Docker Desktop

### Step 1 – Start Kafka infrastructure
```bash
docker-compose up -d
```
Wait ~30 s. Browse topics at http://localhost:8989.

### Step 2 – Build all modules
```bash
mvn clean install -DskipTests
```

### Step 3 – Start services in order

```bash
# Terminal 1 – must start first
cd service-registry && mvn spring-boot:run

# Terminal 2
cd api-gateway && mvn spring-boot:run

# Terminals 3-5 (any order, after registry is up)
cd product-service      && mvn spring-boot:run
cd cart-service         && mvn spring-boot:run
cd wishlist-service     && mvn spring-boot:run

# Terminals 6-8
cd order-service        && mvn spring-boot:run
cd payment-service      && mvn spring-boot:run
cd notification-service && mvn spring-boot:run

# Terminal 9 (last)
cd frontend-service     && mvn spring-boot:run
```

### Step 4 – Open the store
```
http://localhost:8090
```

---

## 🔗 Useful URLs

| URL | Description |
|-----|-------------|
| http://localhost:8090 | Store frontend |
| http://localhost:8761 | Eureka dashboard |
| http://localhost:8989 | Kafka UI |
| http://localhost:8080/actuator/health | Gateway health |
| http://localhost:8081/swagger-ui.html | Product API docs |
| http://localhost:8082/swagger-ui.html | Cart API docs |
| http://localhost:8083/swagger-ui.html | Wishlist API docs |
| http://localhost:8084/swagger-ui.html | Order API docs |
| http://localhost:8085/swagger-ui.html | Payment API docs |
| http://localhost:8086/swagger-ui.html | Notification API docs |
| http://localhost:808x/h2-console | H2 console per service |

---

## 🧪 Testing the Saga

1. Browse and add items to cart.
2. Click **Pay Now** (card / UPI / COD).
3. Check order status: `GET http://localhost:8080/api/orders`
4. Check notifications: `GET http://localhost:8080/api/notifications`
5. Watch Kafka UI at http://localhost:8989 to see events flowing.

**Simulate payment failure (Card only):**
Set cart total to an amount ending in digit `3` (e.g., ₹13, ₹23, ₹113).
Order will be set to `CANCELLED`.

---

## 📁 Project Structure

```
e-commerce/
├── pom.xml                   ← parent multi-module POM
├── docker-compose.yml        ← Kafka + Zookeeper + Kafka UI
├── common/                   ← shared Kafka event classes + topic constants
├── service-registry/         ← Eureka server (port 8761)
├── api-gateway/              ← Spring Cloud Gateway (port 8080)
├── product-service/          ← product catalog (port 8081)
├── cart-service/             ← shopping cart (port 8082)
├── wishlist-service/         ← wishlist (port 8083)
├── order-service/            ← orders + Saga + Outbox (port 8084)
├── payment-service/          ← payments + Outbox (port 8085)
├── notification-service/     ← notifications Kafka consumer (port 8086)
└── frontend-service/         ← Thymeleaf UI (port 8090)
```
