# API Gateway Pattern

## What is an API Gateway?

An **API Gateway** is a **single entry point** for all client requests. Instead of clients calling services directly, they call the gateway, which routes requests to appropriate services.

```
TRADITIONAL (No Gateway - Tight Coupling):
┌─────────────┬────────────┬──────────────┐
│   Client 1  │ Client 2   │   Client 3   │
└──────┬──────┴────┬───────┴───────┬──────┘
       │           │               │
       ├──────────→ Product Service
       ├──────────→ Cart Service
       ├──────────→ Order Service
       ├──────────→ Payment Service
       └──────────→ Notification Service

Problems:
- Clients know about all service URLs
- Add new service → update all clients
- Hard to track which client calls what
- Hard to add cross-cutting concerns (auth, logging)
```

```
WITH API GATEWAY (Decoupled):
┌─────────────┬────────────┬──────────────┐
│   Client 1  │ Client 2   │   Client 3   │
└─────────┬───┴────┬───────┴─────┬────────┘
          │        │             │
          └────────┴─────────────┘
                   │
          ┌────────▼────────┐
          │   API Gateway   │
          │  (port 8080)    │
          └────────┬────────┘
                   │
       ┌───────────┼───────────┬──────────────┐
       │           │           │              │
       ▼           ▼           ▼              ▼
  Product Srvc  Cart Srvc  Order Srvc  Payment Srvc

Benefits:
- Clients only know gateway URL
- Add service without updating clients
- Centralized authentication, logging, rate limiting
- Service instances transparent to clients
```

---

## API Gateway Responsibilities

### 1. **Routing** – Directing requests to appropriate service

```yaml
routes:
  - id: product-service
    uri: lb://product-service
    predicates:
      - Path=/api/products/**
  
  - id: cart-service
    uri: lb://cart-service
    predicates:
      - Path=/api/cart/**
```

Request to `/api/products/1` → routes to product-service  
Request to `/api/cart` → routes to cart-service

### 2. **Load Balancing** – Distributing requests across instances

```
Request 1 ──┐
Request 2 ──┤
Request 3 ──┤     ┌─────────────────────┐
            ├────→│ product-service-1   │
Request 4 ──┤     ├─────────────────────┤
Request 5 ──┤     │ product-service-2   │
Request 6 ──│     ├─────────────────────┤
            └────→│ product-service-3   │
                  └─────────────────────┘
  (Round-robin, random, weighted, etc.)
```

### 3. **Authentication & Authorization** – Centralized security

```
Request with token
    ↓
Gateway validates token
    ↓
Token valid? → Forward to service
Token invalid? → Return 401 Unauthorized
```

### 4. **Rate Limiting** – Prevent abuse

```
Gateway counts requests per client
    ↓
Exceed limit? → Return 429 Too Many Requests
Within limit? → Forward request
```

### 5. **Circuit Breaking** – Prevent cascading failures

```
If payment-service keeps failing:
    ↓
Gateway circuit opens
    ↓
New requests immediately fail (fallback response)
    ↓
Saves resources, alerts team
```

### 6. **Request/Response Transformation**

```
Client sends: {"product_id": 1}
    ↓
Gateway transforms: {"productId": 1}
    ↓
Service receives transformed request

Response from service: [{"id": 1, ...}]
    ↓
Gateway transforms: {"status": "ok", "data": [...]}
    ↓
Client receives transformed response
```

### 7. **Logging & Monitoring** – Central observation

```
All requests flow through gateway
    ↓
Log all requests in one place
    ↓
Easy to analyze patterns, debug issues
```

---

## Spring Cloud Gateway (Our Implementation)

Spring Cloud Gateway is a **non-blocking, reactive** (WebFlux) API Gateway.

### Configuration

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
      
      routes:
        - id: product-service
          uri: lb://product-service      # Load-balanced via Eureka
          predicates:
            - Path=/api/products/**
          filters:
            - name: CircuitBreaker
              args:
                name: productCircuitBreaker
                fallbackUri: forward:/fallback/product
```

### Predicates (When to Route)

```yaml
predicates:
  - Path=/api/products/**           # Match URL path
  - Method=GET,POST                 # Match HTTP method
  - Host=example.com                # Match domain
  - Query=name,value                # Match query param
  - Header=X-Auth-Token             # Match header
  - Before=2030-12-31T23:59:59Z    # Match before date
```

### Filters (How to Transform)

```yaml
filters:
  - StripPrefix=1                           # Remove prefix
  - AddRequestHeader=X-Request-ID, 123      # Add header
  - AddResponseHeader=X-Response-ID, 456    # Add response header
  - name: CircuitBreaker                    # Resilience
    args:
      name: myCircuitBreaker
      fallbackUri: forward:/fallback
```

---

## API Gateway in Our Project

### Flow

```
Browser (port 8090)
    │ http://localhost:8080/api/products
    ▼
API Gateway (port 8080)
    │ Checks route predicates
    │ Path=/api/products/** matches!
    │ CircuitBreaker filter applied
    │ Load balancer discovers product-service
    │
    ├─→ product-service instance 1 (if available)
    ├─→ product-service instance 2 (fallback)
    │
    ▼ HTTP response
Browser
```

### Configuration

```yaml
# api-gateway/application.yml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "*"          # Frontend (8090) can call us (8080)
            allowedMethods: "*"
            allowedHeaders: "*"
      
      routes:
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/filters
          filters:
            - name: CircuitBreaker
              args:
                name: productCircuitBreaker
                fallbackUri: forward:/fallback/product
        
        - id: cart-service
          uri: lb://cart-service
          predicates:
            - Path=/api/cart/**
          filters:
            - name: CircuitBreaker
              args:
                name: cartCircuitBreaker
                fallbackUri: forward:/fallback/cart
        
        # ... more routes for other services
```

### Fallback Controller

```java
@RestController
public class FallbackController {
    
    @RequestMapping("/fallback/product")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> productFallback() {
        return Mono.just(Map.of("message", 
            "Product service is temporarily unavailable. Please try again later."));
    }
    
    @RequestMapping("/fallback/cart")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> cartFallback() {
        return Mono.just(Map.of("message", 
            "Cart service is temporarily unavailable. Please try again later."));
    }
}
```

When circuit breaker trips (service down), fallback returns user-friendly message instead of error.

---

## Request Lifecycle Through Gateway

```
1. Browser: GET http://localhost:8080/api/cart
   ↓
2. Gateway receives request
   ↓
3. Gateway matches route:
   - Path predicate: /api/cart/** matches!
   - Route ID: cart-service
   ↓
4. Gateway applies filters (in order):
   - CircuitBreaker filter: Check if circuit is CLOSED
   - Circuit is CLOSED (service healthy)
   ↓
5. Gateway does load balancing:
   - Queries Eureka: Where is cart-service?
   - Eureka returns: cart-service at localhost:8082
   ↓
6. Gateway proxies request:
   - Forwards to: http://localhost:8082/api/cart
   ↓
7. CartService processes, returns response
   ↓
8. Gateway returns response to browser
   ↓
9. Browser renders
```

---

## Circuit Breaker on Gateway

```yaml
resilience4j:
  circuitbreaker:
    instances:
      cartCircuitBreaker:
        slidingWindowSize: 10           # Look at last 10 calls
        failureRateThreshold: 50        # If 50%+ fail, open circuit
        waitDurationInOpenState: 10s    # Wait 10s before trying again
        permittedNumberOfCallsInHalfOpenState: 3  # Try 3 calls to recover
```

State transitions:

```
CLOSED (normal)
    │ 50%+ failures
    ▼
OPEN (fail fast, return fallback)
    │ after 10s
    ▼
HALF_OPEN (try recovery with 3 calls)
    │
    ├─ All succeed → back to CLOSED
    └─ Any fails → back to OPEN
```

---

## CORS (Cross-Origin Resource Sharing)

```yaml
globalcors:
  corsConfigurations:
    '[/**]':                           # All paths
      allowedOrigins: "*"              # All origins
      allowedMethods: "*"              # GET, POST, DELETE, etc.
      allowedHeaders: "*"              # All headers
```

Why needed? Frontend (port 8090) and Gateway (port 8080) are different origins. Without CORS, browser blocks request!

---

## Gateway vs Service Implementation

| Aspect | API Gateway | Each Service |
|--------|-------------|--------------|
| Entry point | ✓ Yes | — |
| Authentication | ✓ Often | Can also |
| Rate limiting | ✓ Global limit | Can do local |
| Circuit breaker | ✓ Upstream | ✓ Also own |
| CORS | ✓ Handles | — |
| Business logic | ✗ Never | ✓ Yes |
| Logging | ✓ All requests | ✓ Detailed logs |

---

## Routing Alternatives to Gateway

### Option 1: API Gateway (What We Use)
```
Client → Gateway → Services
```
Centralized routing, single point of contact.

### Option 2: Service Mesh (Istio, Linkerd)
```
Client → Service A (with sidecar proxy)
               ↓ (sidecar handles routing)
         Service B (with sidecar proxy)
```
Decentralized, each service manages own routing.

### Option 3: Load Balancer (AWS ELB, nginx)
```
Client → Load Balancer → Service instances
```
Simpler, no intelligent routing.

---

## Real-World Analogy

API Gateway is like a **hotel concierge**:

- Guests (clients) ask concierge (gateway) for services
- Concierge knows all services available
- Concierge directs guest to appropriate service
- Concierge can check if service is available
- If service is busy, concierge has fallback plan (alternative service)
- Concierge logs all requests

---

## Further Reading

- **Spring Cloud Gateway**: https://spring.io/projects/spring-cloud-gateway
- **Netflix Zuul**: https://github.com/Netflix/zuul
- **AWS API Gateway**: https://aws.amazon.com/api-gateway/

---

**Next**: Learn about circuit breakers → [Circuit Breaker Pattern](./09-circuit-breaker-pattern.md)  
**Or**: Learn about routing with Feign → [OpenFeign & Synchronous Calls](./10-openfeign-sync-calls.md)

