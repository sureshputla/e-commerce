# Circuit Breaker Pattern

## The Problem

In microservices, services call each other. If one service is slow or down, it can cascade failures:

```
Request to OrderService
    │
    └─→ OrderService calls CartService (slow...)
            │
            └─→ CartService calls ProductService (down!)
                    │
                    └─→ ProductService doesn't respond

OrderService's request hangs, waiting...
Queue of requests builds up
All threads blocked
OrderService eventually crashes

CASCADING FAILURE! 💥
```

### Cascading Failure Chain

```
Service A slow Down
    ↓
Service B's requests to A timeout
    ↓
Service B's thread pool exhausted
    ↓
Service B becomes slow
    ↓
Service C times out calling B
    ↓
Service C crashes
    ↓
Entire system down
```

**Solution**: Circuit Breaker – stop calling failing services!

---

## Circuit Breaker Pattern

Inspired by **real electrical circuit breakers** that stop power flow when overloaded.

```
Electrical Breaker:           Software Circuit Breaker:
Normal: current flows          CLOSED: requests pass through
Overload: switch opens         OPEN: requests fail fast
         (stops power)                (no slow waiting)
Cool down: switch resets       HALF_OPEN: test recovery
           (restores power)            (try one request)
```

### Three States

```
┌──────────────────────────────────────────────────┐
│                   CLOSED (Normal)                │
│  ✓ Requests flow to service                      │
│  ✓ Service responding                            │
│  ✓ No protection needed                          │
│                                                   │
│  Failure Rate > threshold?  YES ─────────┐      │
└───────────────────────────────────────────│──────┘
                                            │
                                            ▼
                ┌──────────────────────────────────────────────┐
                │              OPEN (Failed)                   │
                │  ✗ Requests fast-fail (return fallback)      │
                │  ✗ Service likely unhealthy                  │
                │  ✗ Save service from overload                │
                │                                              │
                │  Wait timeout? YES ────────────────┐        │
                └───────────────────────────────────────│──────┘
                                                        │
                                                        ▼
                         ┌──────────────────────────────────────────────┐
                         │         HALF_OPEN (Recovering)              │
                         │  ⚠ Try one request to test service           │
                         │  ⚠ If succeeds → back to CLOSED             │
                         │  ⚠ If fails → back to OPEN                 │
                         │                                             │
                         │  Success? ┌─ YES ─→ CLOSED                 │
                         │           │                                │
                         │           └─ NO ──→ OPEN                  │
                         └──────────────────────────────────────────────┘
```

---

## Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      productCircuitBreaker:
        registerHealthIndicator: true
        slidingWindowSize: 10              # Look at last 10 calls
        minimumNumberOfCalls: 5            # Need 5+ calls to decide
        permittedNumberOfCallsInHalfOpenState: 3  # Try 3 when recovering
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 10s       # Wait 10s before retry
        failureRateThreshold: 50           # If 50%+ fail, trip open
        eventConsumerBufferSize: 10
```

### Parameters Explained

```
slidingWindowSize: 10
  → Look at the last 10 requests
  → If 5+ fail (50%), open circuit

minimumNumberOfCalls: 5
  → Don't act on fewer than 5 calls
  → Prevents false positives

waitDurationInOpenState: 10s
  → Stay OPEN for 10 seconds
  → Then try recovery (HALF_OPEN)

permittedNumberOfCallsInHalfOpenState: 3
  → In HALF_OPEN, allow 3 test requests
  → If all succeed, full recovery
  → If any fails, back to OPEN
```

---

## In Our Project

### API Gateway Circuit Breakers

```yaml
# Each route has its own circuit breaker
routes:
  - id: product-service
    uri: lb://product-service
    predicates:
      - Path=/api/products/**
    filters:
      - name: CircuitBreaker
        args:
          name: productCircuitBreaker
          fallbackUri: forward:/fallback/product
```

When `productCircuitBreaker` opens:
1. New requests to `/api/products/**` immediately return fallback
2. HTTP 503 + message: "Product service is temporarily unavailable"
3. No waiting, no thread exhaustion!

### Feign Client Circuit Breakers

```java
@FeignClient(
    name = "product-service",
    fallback = ProductServiceClientFallback.class
)
public interface ProductServiceClient {
    @GetMapping("/api/products/{id}")
    ResponseEntity<ProductDto> getProduct(@PathVariable Integer id);
}

@Component
public class ProductServiceClientFallback implements ProductServiceClient {
    @Override
    public ResponseEntity<ProductDto> getProduct(Integer id) {
        // Return placeholder when service unavailable
        return ResponseEntity.ok(ProductDto.builder()
            .id(id)
            .name("Product Unavailable")
            .build());
    }
}
```

Feign automatically applies circuit breaker logic:
1. Call product-service → fails
2. Failure count increases
3. When threshold exceeded → circuit opens
4. Future calls return fallback immediately

---

## Fallback Strategies

### Strategy 1: Return Cached Data

```java
public CartResponse getCart() {
    try {
        return cartServiceClient.getCart();
    } catch (Exception e) {
        return cachedCart;  // Return last known good state
    }
}
```

### Strategy 2: Return Degraded Response

```java
public ProductResponse getProduct(int id) {
    try {
        return productServiceClient.getProduct(id);
    } catch (Exception e) {
        return ProductResponse.builder()
            .id(id)
            .name("Product details temporarily unavailable")
            .price(0)
            .build();
    }
}
```

### Strategy 3: Return Empty/Null

```java
public List<Product> getBestSellers() {
    try {
        return productServiceClient.getBestSellers();
    } catch (Exception e) {
        return Collections.emptyList();  // Calm degradation
    }
}
```

### Strategy 4: Fail Fast

```java
public Order checkout() {
    if (circuitBreakerOpen()) {
        throw new ServiceUnavailableException(
            "Order service temporarily unavailable");
    }
    // ...
}
```

---

## Monitoring Circuit Breaker States

### Via Actuator

```bash
curl http://localhost:8080/actuator/health
```

Returns:

```json
{
  "status": "UP",
  "components": {
    "circuitbrekers": {
      "status": "UP",
      "details": {
        "productCircuitBreaker": {
          "status": "CLOSED",  // or OPEN, HALF_OPEN
          "details": {
            "failureRate": "-1.0%",
            "slowCallRate": "-1.0%",
            "numberOfBufferedCalls": 0,
            "numberOfFailedCalls": 0,
            "numberOfSlowCalls": 0,
            "numberOfSuccessfulCalls": 0
          }
        },
        "cartCircuitBreaker": {
          "status": "CLOSED"
        }
      }
    }
  }
}
```

### Via Metrics

Prometheus metrics for monitoring:

```
resilience4j_circuitbreaker_state{name="productCircuitBreaker"} 0  // 0=CLOSED, 1=OPEN, 2=HALF_OPEN
resilience4j_circuitbreaker_calls_total{...} 150
resilience4j_circuitbreaker_calls_total{...failed...} 12
```

---

## Retry vs Circuit Breaker

### Retry

```
Request fails  ─────┐
    ↓               │
Wait 100ms          │ up to 3 times
    ↓               │
Try again  ─────────┘
    ↓
Success or give up
```

Good for: Transient failures (network hiccup)

### Circuit Breaker

```
Request fails ────┐
    ↓             │
Count failure     │ after 5+ failures
    ↓             │
Open circuit ─────┘
    ↓
Fail immediately (no wait!)
```

Good for: Service down (don't waste time retrying)

**Best**: Use together!

```java
@Retry(name = "productRetry")
@CircuitBreaker(name = "productCircuitBreaker", fallbackMethod = "fallback")
public Product getProduct(int id) {
    return productServiceClient.getProduct(id);
}

public Product fallback(int id) {
    return Product.builder().name("Unavailable").build();
}
```

1st attempt fails → retry (3 times)
After 3+ total failures → circuit opens → future calls fail immediately

---

## Real-World Analogy

Circuit Breaker is like a **restaurant reservation system**:

- **CLOSED**: Restaurant accepting reservations
- Popularity spikes → people wait (overload risk)
- **OPEN**: Restaurant temporarily stops taking reservations
- Asks you to call back later
- Avoids wasting time for people who can't be seated anyway
- **HALF_OPEN**: "OK, let's try accepting one more reservation to test"
  - If it works → back to accepting all
  - If it doesn't → go back to closed

---

## Common Mistakes

❌ **Too sensitive** – Opens on minor failures
```yaml
failureRateThreshold: 10  # Opens if only 10% fail – too aggressive!
```

❌ **Too lenient** – Waits too long
```yaml
waitDurationInOpenState: 5m  # 5 minutes –users suffer too long!
```

❌ **No fallback** – Returns error instead of degraded response
```java
catch (Exception e) {
    throw e;  // ❌ Bad! Let user see error
    // ✅ Good:
    // return cachedData or emptyResponse
}
```

❌ **Cascading timeouts** – Doesn't help if timeout is too long
```yaml
timeout: 30s  # ❌ Too long! Threads still blocked
timeout: 5s   # ✓ Reasonable
```

---

## Further Reading

- **Resilience4j Documentation**: https://resilience4j.readme.io/
- **Martin Fowler - Circuit Breaker**: https://martinfowler.com/bliki/CircuitBreaker.html
- **Release It! by Michael T. Nygard**: Classic book on stability patterns

---

**Next**: Learn about async messaging → [Apache Kafka & Event-Driven Architecture](./11-kafka-event-driven.md)

