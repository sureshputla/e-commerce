# Notification Service

Listens to `payment.processed` Kafka events and persists notification records. Simulates email/SMS notifications via log output.

## Port
`8086`

## Technologies
- Spring Boot 3.3.5 (Web + JPA)
- H2 In-Memory Database
- Lombok
- Spring Kafka (consumer)
- Spring Cloud Eureka Client
- Springdoc OpenAPI

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET    | `/api/notifications` | Get all notifications for the current user |

## Kafka Consumers

| Topic | Action |
|-------|--------|
| `payment.processed` | Saves a `Notification` record; logs 📧 confirmation or cancellation |

## Notification Types

| Type | Trigger |
|------|---------|
| `PAYMENT_SUCCESS` | Payment succeeded → order confirmed |
| `PAYMENT_FAILED`  | Payment failed → order cancelled |

## Extending to Real Notifications
Replace the `log.info/warn` in `NotificationService` with:
- **Email:** Spring Mail (`spring-boot-starter-mail` + JavaMailSender)
- **SMS:** Twilio SDK
- **Push:** Firebase Cloud Messaging

## Run
```bash
# Prerequisites: service-registry, Kafka
cd notification-service
mvn spring-boot:run
```

## Useful URLs
| URL | Description |
|-----|-------------|
| http://localhost:8086/swagger-ui.html | Swagger UI |
| http://localhost:8086/h2-console | H2 Console (`jdbc:h2:mem:notificationdb`) |
| http://localhost:8080/api/notifications | Via Gateway |

