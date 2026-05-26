# Cart Service

Manages the shopping cart. Persists items per user and reacts to order events via Kafka to clear the cart automatically.

## Port
`8082`

## Technologies
- Spring Boot 3.3.5 (Web + JPA)
- H2 In-Memory Database
- Lombok
- Spring Cloud OpenFeign + LoadBalancer (calls product-service)
- Resilience4j (Feign circuit breaker + fallback)
- Spring Kafka (consumer: `order.created`)
- Spring Cloud Eureka Client
- Springdoc OpenAPI

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET    | `/api/cart` | Get current cart (items + total) |
| POST   | `/api/cart/{productId}` | Add product to cart (increments quantity if present) |
| DELETE | `/api/cart/{productId}` | Remove product from cart |

Response shape (backward-compatible with original monolith):
```json
{
  "items": [
    { "product": { "id": 1, "name": "...", "price": 799 }, "quantity": 2, "subtotal": 1598 }
  ],
  "total": 1598
}
```

## Kafka Consumers
| Topic | Action |
|-------|--------|
| `order.created` | Clears the cart for the user in the event |

## Inter-Service Calls (Feign)
| Service | Endpoint | Purpose |
|---------|----------|---------|
| product-service | `GET /api/products/{id}` | Fetch product name + price to denormalize in cart item |

## Circuit Breaker Fallback
If `product-service` is unavailable, `ProductServiceClientFallback` returns a placeholder product so the cart can still partially function.

## Run
```bash
# Prerequisites: service-registry, product-service, Kafka (docker-compose up -d)
cd cart-service
mvn spring-boot:run
```

## Useful URLs
| URL | Description |
|-----|-------------|
| http://localhost:8082/swagger-ui.html | Swagger UI |
| http://localhost:8082/h2-console | H2 Console (`jdbc:h2:mem:cartdb`) |

