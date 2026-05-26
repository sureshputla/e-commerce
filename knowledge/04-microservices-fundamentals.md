# Microservices Fundamentals

## What are Microservices?

**Microservices** is an architectural style where a large application is **decomposed into small, independent services** that:
- Run in separate processes
- Communicate via network protocols (HTTP, messaging)
- Own their own database
- Are independently deployable
- Are organized around business capabilities

### Monolith vs Microservices

**Monolithic Architecture**:
```
┌─────────────────────────────┐
│   Single Application        │
├─────────────────────────────┤
│ Auth Module                 │
│ Product Module              │
│ Cart Module                 │
│ Order Module                │
│ Payment Module              │
│ Notification Module         │
└─────────────────────────────┘
     ↓ Single Database
```

One code base, one deployment, one database, everything tightly coupled.

**Microservices Architecture**:
```
┌─────────────┐  ┌──────────┐  ┌────────┐
│ Auth Srvc   │  │ Product  │  │ Cart   │
│             │  │ Service  │  │ Service│
└─────────────┘  └──────────┘  └────────┘
     │ DB1            │ DB2       │ DB3
     ▼                ▼           ▼
  [SQLite]         [H2]      [PostgreSQL]

     ↓ api-gateway ↓
    Multiple independent services, each with own database
```

Each service is independently deployable, scalable, and maintainable.

---

## Original Monolith Code

```
src/main/java/com.sureshputla.ecommerce/
├── controller/
│   ├── HomeController.java     ← UI
│   └── StoreApiController.java ← API for products, cart, orders
├── model/
│   ├── Product.java
│   └── CartItem.java
└── service/
    ├── ProductCatalogService.java
    └── StoreService.java
```

**Problem**: Everything is in one process. If cart service crashes, product service also crashes.

---

## Our Microservices Decomposition

We decomposed the monolith into **9 independent services**:

```
Original responsibility mapping:

┌─ ProductCatalogService ──→ product-service (8081)
│
├─ StoreService (cart)    ──→ cart-service (8082)
│
├─ StoreService (wishlist)─→ wishlist-service (8083)
│
├─ StoreService (orders)  ──→ order-service (8084)
│
└─ (new) PaymentService   ──→ payment-service (8085)
         NotificationSvc  ──→ notification-service (8086)
```

---

## Principles of Microservices

### 1. **Single Responsibility**

Each service owns one business capability.

> **OrderService** – only handles orders  
> **PaymentService** – only handles payments  
> **ProductService** – only handles products  

NOT:

> **Service** – handles everything

### 2. **Autonomous**

Services are **independent deployable units**.

```
Deploy order-service without affecting cart-service
```

### 3. **Own Data**

Each service owns its own database – no shared database.

```
❌ WRONG (tight coupling):
order_service ──┐
                ├─→ Shared Database ❌
cart_service ───┘

✅ RIGHT (decoupled):
order_service ──→ order_db
cart_service ──→ cart_db
```

Why? Prevents cascading failures. If cart_db goes down, order_db still works.

### 4. **Communicate via Networks**

Services call each other via HTTP, gRPC, or message queues – **never direct method calls**.

```java
// ❌ WRONG (monolith tight coupling):
class OrderService {
    private CartService cartService = new CartService();
    cartService.getCart();  // Direct method call – tight coupling!
}

// ✅ RIGHT (microservice decoupling):
@FeignClient(name = "cart-service")
interface CartServiceClient {
    @GetMapping("/api/cart")
    CartResponse getCart();
}
// Network call – loosely coupled!
```

### 5. **Independently Scalable**

Scale only the services you need.

```
If cart-service gets 1000x traffic:
  → Deploy 100 instances of cart-service
  → product-service stays at 1 instance
  → Not wasteful!

In monolith:
  → Must scale entire app
  → Wasteful!
```

---

## Microservices Challenges

### Challenge 1: **Distributed Transactions**

**In monolith** (simple):
```java
@Transactional
public void placeOrder(Cart cart) {
    Order order = createOrder(cart);      // DB save
    Payment payment = processPayment(...); // DB save
    updateInventory(...);                  // DB save
    // All or nothing – ACID guaranteed by single TX
}
```

**In microservices** (hard):
```
OrderService → saves order
PaymentService → must process payment
InventoryService → must update inventory

What if PaymentService fails after OrderService commits?
→ Order saved, but payment not processed!
→ Inconsistent state!
```

**Solution**: Choreography Saga (see [07-choreography-saga.md](./07-choreography-saga.md))

### Challenge 2: **Distributed Tracing**

**In monolith**: All logs in one place – easy to trace.

**In microservices**:
```
Request comes to gateway (8080)
→ routed to order-service (8084) - makes request
→ calls cart-service (8082) - makes request
→ calls product-service (8081)

One request spawns multiple service calls.
Logs scattered across multiple services!
How do you trace one request?
```

**Solution**: Correlation IDs, distributed tracing (Sleuth + Zipkin)

### Challenge 3: **Network Calls are Slow**

```
Method call: 0.001 ms
Network call over HTTP: 10-100 ms

100x slower!
Must handle failures gracefully.
```

**Solution**: Circuit breakers (see [09-circuit-breaker-pattern.md](./09-circuit-breaker-pattern.md))

### Challenge 4: **Data Consistency**

**In monolith**: ACID guaranteed by database.

**In microservices**: Each service has own DB!
```
Order created: ✓
Payment processed: ✓
But notification service offline: ✗

Partial success = eventual consistency
```

**Solution**: Outbox Pattern + event-driven architecture (see [08-transactional-outbox.md](./08-transactional-outbox.md))

---

## Microservices Benefits

✅ **Independent scaling** – scale only what you need  
✅ **Independent deployment** – deploy one service without others  
✅ **Technology choice** – each service can use different tech (Java, Python, Go)  
✅ **Fault isolation** – one service crash doesn't crash others  
✅ **Team independence** – different teams own different services  
✅ **Organizational alignment** – services match organizational structure (Conway's Law)  

---

## Microservices Drawbacks

❌ **Operational complexity** – monitoring many services, debugging distributed systems  
❌ **Network failures** – more network calls = more failure points  
❌ **Eventual consistency** – no instant ACID transactions  
❌ **Data duplication** – may denormalize data across services  
❌ **Deployment complexity** – orchestrating multiple deployments  
❌ **Higher latency** – network calls are slower than method calls  

---

## When to Use Microservices

**✅ USE when**:
- Large team (10+ developers)
- App has distinct business domains
- Need independent scaling
- Need independent deployment
- Different services need different tech stacks
- Organization is distributed

**❌ DON'T USE when**:
- Small team (<5 developers)
- Simple application
- Everything tightly coupled
- High-performance requirements (need method calls, not network)
- Don't understand distributed systems yet

---

## In Our Project

### From Monolith

**Single jar** containing everything:
```
EcommerceApplication.jar
├── ProductCatalogService
├── StoreService (cart)
├── StoreService (wishlist)
├── StoreService (orders)
└── Single Database H2
```

### To Microservices

**9 independent jars**:
```
product-service.jar      ──→ port 8081 ──→ productdb
cart-service.jar         ──→ port 8082 ──→ cartdb
wishlist-service.jar     ──→ port 8083 ──→ wishlistdb
order-service.jar        ──→ port 8084 ──→ orderdb
payment-service.jar      ──→ port 8085 ──→ paymentdb
notification-service.jar ──→ port 8086 ──→ notificationdb
frontend-service.jar     ──→ port 8090 ──→ (no db)
api-gateway.jar          ──→ port 8080 ──→ (no db)
service-registry.jar     ──→ port 8761 ──→ (in-memory)
```

Each can be:
- **Deployed independently** – update one service without redeploying all
- **Scaled independently** – if cart-service gets 1000x traffic, scale only that
- **Written in different languages** – product-service in Java, payment-service in Python
- **Maintained by different teams**

---

## Service Coordination

### Synchronous (Request-Response)

```
Frontend ──HTTP──> API Gateway
                      ↓
                  Cart Service ──Feign──> Product Service
                      ↑
                  Return response
```

Used for: Immediate responses needed.

### Asynchronous (Event-Driven)

```
Order Service ──Publish──> Kafka Topic "order.created"
                                    ↓
                         ┌──────────┴──────────┐
                         ↓                     ↓
                   Payment Service      Cart Service
                   (consumes)            (consumes)
```

Used for: Fire-and-forget, eventual consistency.

---

## Real-World Analogy

**Monolith**: A restaurant where one chef does everything
- Cook appetizers, main, dessert
- If the chef is tired, entire restaurant shuts down
- Hard to scale (need the same chef, not hiring specialists)

**Microservices**: A restaurant with specialized stations
- Appetizer chef ↔ Main course chef ↔ Dessert chef
- If appetizer chef is sick, other stations keep running
- Easy to scale (hire more appetizer chefs if needed)
- But coordination is harder (communication between stations)

---

## Further Reading

- **Sam Newman - Building Microservices**: Industry bible
- **Martin Fowler Microservices**: https://martinfowler.com/articles/microservices.html
- **Chris Richardson - Microservices Patterns**: https://microservices.io/

---

**Next**: Learn how services discover each other → [Service Discovery (Eureka)](./05-service-discovery-eureka.md)

