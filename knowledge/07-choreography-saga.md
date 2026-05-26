# Choreography Saga Pattern

## What is a Saga?

A **Saga** is a pattern for managing **distributed transactions** across multiple services. When a transaction spans multiple services, a Saga coordinates the steps.

### The Problem

**In monolith** (simple - single transaction):
```java
@Transactional
public void placeOrder(Cart cart) {
    Order order = createOrder(cart);      // DB save
    Payment payment = processPayment(...); // DB save  
    clearCart(cart);                       // DB save
    // All or nothing! One DB, ACID guaranteed.
}
```

All three operations succeed together, or all rollback. The database handles the transaction.

**In microservices** (hard - no single transaction):
```
OrderService creates order          (saves to order_db)
    ↓
PaymentService processes payment    (saves to payment_db)
    ↓
CartService clears cart             (saves to cart_db)

But PaymentService is offline!
Order already saved!
Inconsistent state!
```

Three separate databases = no ACID transaction. How do we maintain consistency?

---

## Saga Patterns

### Pattern 1: Choreography (What We Use)

Services react to **events**. No central orchestrator.

```
1. POST /api/checkout
   ↓
2. OrderService creates Order + publishes "order.created"
   ↓
3. PaymentService listens to "order.created" → processes payment → publishes "payment.processed"
   ↓
4. OrderService listens to "payment.processed" → updates order status
   ↓
5. CartService listens to "order.created" → clears cart
   ↓
6. NotificationService listens to "payment.processed" → saves notification
```

Each service independently listens and reacts!

### Pattern 2: Orchestration (Alternative)

Central orchestrator coordinates all steps.

```
┌─────────────────────────┐
│ Saga Orchestrator       │
│ (coordinates flow)      │
├─────────────────────────┤
│ 1. Call OrderService   │
│ 2. Call PaymentService │
│ 3. Call CartService    │
└──────────┬──────────────┘
           │
    ┌──────┼──────┬──────────┐
    ▼      ▼      ▼          ▼
Order  Payment  Cart   Notification
```

Orchestrator is single point of control but single point of failure.

---

## Our Choreography Flow

```
User clicks "Pay Now"
    │
    ▼
POST /api/checkout
    │
    ▼ (gateway routes to order-service)
OrderService.checkout()
    │
    ├─ 1. Fetch cart from cart-service (Feign)
    │
    ├─ 2. Create Order entity (status=PENDING)
    │
    ├─ 3. Save OrderCreatedEvent to OUTBOX (same DB transaction as order!)
    │    ├── Event stored atomically with order
    │    └── Guarantees: if order saved, event exists
    │
    ├─ 4. Return response: "Order placed, processing payment..."
    │
    └─ 5. OutboxPublisher (@Scheduled every 5s)
         ├─ Polls outbox_events table
         ├─ Finds "order.created" event
         ├─ Publishes to Kafka topic "order.created"
         └─ Marks event as PUBLISHED
            │
            ▼ (Kafka topic "order.created")
            │
            ├─→ PaymentService listens
            │   ├─ Processes payment  
            │   ├─ Saves PaymentProcessedEvent to outbox
            │   └─ OutboxPublisher publishes to Kafka topic "payment.processed"
            │
            └─→ CartService listens
                ├─ Clears user's cart
                └─ Done
                   │
                   ▼ (Kafka topic "payment.processed")
                   │
                   ├─→ OrderService listens
                   │   ├─ if payment succeeded → order.status = CONFIRMED
                   │   └─ if payment failed → order.status = CANCELLED
                   │
                   └─→ NotificationService listens
                       ├─ Saves notification record
                       └─ Logs 📧 email notification
```

---

## Choreography vs Orchestration

| Aspect | Choreography | Orchestration |
|--------|--------------|---------------|
| **Central controller** | ✗ None | ✓ Yes |
| **Service autonomy** | ✓ High | ✗ Low |
| **Debugging** | ✗ Hard | ✓ Easy |
| **Complexity** | ✓ Simple | ✗ Complex |
| **Coupling** | ✗ Medium (via events) | ✓ Tight (to orchestrator) |
| **Scalability** | ✓ Good | ✗ Orchestrator bottleneck |
| **Visibility** | ✗ Implicit flow | ✓ Explicit |

**Our choice**: Choreography – simpler, more resilient, better suited for async.

---

## Saga States

Each service involved in saga maintains state:

```java
public enum OrderStatus {
    PENDING,      // Created, awaiting payment
    CONFIRMED,    // Payment succeeded
    CANCELLED     // Payment failed (compensating action)
}
```

### Happy Path (Success)

```
PENDING → CONFIRMED
  ↓
Order placed + payment succeeded ✓
```

### Sad Path (Failure with Compensation)

```
PENDING → CANCELLED
  ↓
Order placed but payment failed
Compensating action: No cart clear needed (order not confirmed anyway)
```

---

## Compensating Transactions

If something fails, undo previous steps (like rollback).

```
Step 1: Create Order
    ↓
Step 2: Process Payment → FAILS!
    ↓
Compensating Transaction: Mark order as CANCELLED
    ↓
System back to consistent state
```

In our flow:
- **If payment fails** → order.status = CANCELLED (compensating transaction)
- **If all succeeds** → no compensation needed

---

## In Our Project

### 1. OrderService – Saga Initiator

```java
@Service
@Transactional
public class OrderService {
    public OrderResponse checkout(String paymentMethod) {
        // 1. Create Order
        Order order = Order.builder()
            .id(UUID.randomUUID().toString())
            .status(OrderStatus.PENDING)
            .build();
        orderRepository.save(order);
        
        // 2. Save event in SAME transaction (Outbox Pattern)
        OrderCreatedEvent event = buildOrderCreatedEvent(order);
        saveOutboxEvent(order.getId(), event);  // ATOMIC save!
        
        // 3. Return immediately
        return OrderResponse.builder()
            .orderId(order.getId())
            .message("Order placed, processing payment...")
            .build();
    }
}
```

### 2. PaymentService – First Saga Step

```java
@Component
public class OrderEventListener {
    @KafkaListener(topics = "order.created")
    public void onOrderCreated(String payload) {
        OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
        
        // Process payment (simulate: fails if amount ends in 3)
        boolean success = simulatePayment(event);
        
        // Save PaymentProcessedEvent to outbox
        PaymentProcessedEvent result = PaymentProcessedEvent.builder()
            .orderId(event.getOrderId())
            .success(success)
            .build();
        saveOutboxEvent(result);
    }
}
```

### 3. CartService – Clears on Order

```java
@Component
public class OrderEventListener {
    @KafkaListener(topics = "order.created")
    public void onOrderCreated(String payload) {
        OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
        
        // Compensating action: clear cart when order placed
        cartService.clearCart(event.getUserId());
    }
}
```

### 4. OrderService – Updates on Payment

```java
@Component
public class PaymentEventListener {
    @KafkaListener(topics = "payment.processed")
    public void onPaymentProcessed(String payload) {
        PaymentProcessedEvent event = objectMapper.readValue(payload, PaymentProcessedEvent.class);
        
        if (event.isSuccess()) {
            orderService.confirmOrder(event.getOrderId());
        } else {
            orderService.cancelOrder(event.getOrderId());  // Compensating TX
        }
    }
}
```

---

## Testing the Saga

### Happy Path
```
1. Add items to cart
2. Click "Pay Now" (card)
3. Order created (PENDING)
4. PaymentService processes → SUCCESS
5. Order updated (CONFIRMED) ✓
6. Cart cleared ✓
7. Notification saved ✓
```

### Sad Path (Payment Failure)
```
1. Add items to cart
2. Add item so total ends in 3 (e.g., ₹13, ₹23)
3. Click "Pay Now" (card)
4. Order created (PENDING)
5. PaymentService processes → FAILS (simulated)
6. Order updated (CANCELLED) ✓
7. Cart NOT cleared (compensating action) ✓
8. Notification saved ✓
```

---

## Challenges with Choreography Saga

### 1. **Implicit Flow**

Hard to see the entire saga flow. Must read all service code.

### 2. **Cyclical Dependencies**

Services may depend on each other via events.

```
OrderService publishes to "order.created"
CartService listens to "order.created"
But CartService also publishes "cart.cleared"
Which OrderService might listen to?
→ Circular dependency!
```

### 3. **Eventual Consistency**

Data not immediately consistent.

```
Order status updated to PENDING
   ↓ (async Kafka)
Payment processed
   ↓ (async Kafka)
Order status updated to CONFIRMED

During this window, order status might be PENDING
(Not CONFIRMED yet!)
```

### 4. **Debugging Distributed Saga**

Hard to trace failures across services.

---

## Idempotency is Critical

Kafka guarantees **at-least-once** delivery, so services may receive same event multiple times.

```
PaymentService receives "order.created"
    ↓
Creates payment (success) ✓
    ↓
Publishes "payment.processed"
    ↓
But Kafka sends it again (duplicate)
    ↓
PaymentService receives AGAIN
    ↓
Tries to create payment again
    ↓ PROBLEM if not idempotent!
```

**Solution**: Check if already processed!

```java
@Transactional
public void processPayment(OrderCreatedEvent orderEvent) {
    // Idempotency check
    if (paymentRepository.findByOrderId(orderEvent.getOrderId()).isPresent()) {
        log.warn("Payment already processed for order={}", orderEvent.getOrderId());
        return;  // Skip
    }
    
    // Process payment (create new record)
    ...
}
```

---

## Real-World Analogy

Choreography Saga is like a **wedding ceremony**:

- **No central orchestrator** – each person (service) knows their role
- **Bride walks down aisle** (order-created event) ← everyone reacts
- **Groom says vows** ← bride reacts
- **Rings exchanged** ← guests react (notifications)
- No single person coordinating, but everyone knows the flow!

---

## Further Reading

- **Chris Richardson - Saga Pattern**: https://microservices.io/patterns/data/saga.html
- **Event Sourcing**: https://martinfowler.com/eaaDev/EventSourcing.html

---

**Next**: Learn how we reliably publish events via Outbox → [Transactional Outbox Pattern](./08-transactional-outbox.md)

