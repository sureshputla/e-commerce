# API Gateway

Single entry point for all client requests. Routes traffic to downstream services with circuit breakers.

## Port
`8080`

## Technologies
- Spring Cloud Gateway (reactive / WebFlux)
- Resilience4j Circuit Breaker
- Spring Cloud Netflix Eureka Client
- Spring Boot Actuator
- Springdoc OpenAPI (WebFlux)

## Routing Table

| Path Pattern        | Downstream Service  | Circuit Breaker          |
|---------------------|---------------------|--------------------------|
| `/api/filters`      | product-service     | `productCircuitBreaker`  |
| `/api/products/**`  | product-service     | `productCircuitBreaker`  |
| `/api/cart/**`      | cart-service        | `cartCircuitBreaker`     |
| `/api/wishlist/**`  | wishlist-service    | `wishlistCircuitBreaker` |
| `/api/checkout`     | order-service       | `orderCircuitBreaker`    |
| `/api/orders/**`    | order-service       | `orderCircuitBreaker`    |
| `/api/payments/**`  | payment-service     | `paymentCircuitBreaker`  |
| `/api/notifications/**` | notification-service | `notificationCircuitBreaker` |

## Circuit Breaker configuration (per instance)
- Sliding window: 10 calls
- Failure threshold: 50%
- Wait in OPEN state: 10 s
- Fallback: `/fallback/{service}` → HTTP 503 + JSON message

## CORS
Global CORS is enabled allowing all origins – required for the frontend (port 8090) to call the gateway (port 8080).

## Run
```bash
# service-registry must be running first
cd api-gateway
mvn spring-boot:run
```

## Useful URLs
| URL | Description |
|-----|-------------|
| http://localhost:8080/actuator/health | Gateway + circuit breaker health |
| http://localhost:8080/actuator/circuitbreakers | Circuit breaker states |

