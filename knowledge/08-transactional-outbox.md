# Transactional Outbox Pattern

## The Problem

When publishing events to Kafka, two things must succeed together:

```
1. Create Order in database
2. Publish OrderCreatedEvent to Kafka

But they happen in separate systems!
```

### Failure Scenarios

**Scenario 1**: Event published, but DB save fails
```
→ Kafka has event: Order {id: 123, ...}
→ Database doesn't have order
→ Inconsistent! Downstream services think order exists, but it doesn't.
```

**Scenario 2**: DB save succeeds, but Kafka publish fails
```
→ Database has order
→ Event never published
→ Other services never know about order!
```

**Scenario 3**: Kafka temporarily unavailable
```
→ Application crashes waiting for Kafka response
→ Order never created
```

The challenge: **How do we guarantee atomicity when DB and Kafka are separate systems?**

---

## The Solution: Transactional Outbox

Save the event **to the same database** as your business data. Then, a separate component publishes it to Kafka.

```
BEFORE (Two-Phase Commit - Problematic):
┌──────────────────────────────────────┐
│ 1. Create Order in DB              │
│ 2. Publish to Kafka                │ (if step 1 fails, step 2 doesn't run)
└──────────────────────────────────────┘

AFTER (Transactional Outbox):
┌──────────────────────────────────────┐
│ 1. Create Order in DB               │
│ 2. Create OutboxEvent in SAME DB    │ (ATOMIC - both happen or neither)
│    @Transactional                   │
└──────────────────────────────────────┘
        │
        ↓ (Later, async)
┌──────────────────────────────────────┐
│ 3. OutboxPublisher reads table      │
│ 4. Publishes pending events to Kafka│
│ 5. Marks event as PUBLISHED        │
└──────────────────────────────────────┘
```

If anything in step 1-2 fails → entire transaction rolls back. No partial state.
If step 4 fails → event stays PENDING, retry later!

---

## How It Works

### Step 1: Define Outbox Table

```sql
CREATE TABLE outbox_events (
    id VARCHAR(UUID) PRIMARY KEY,
    aggregate_type VARCHAR(50),      -- "Order"
    aggregate_id VARCHAR(UUID),       -- order ID
    event_type VARCHAR(50),           -- Kafka topic
    payload TEXT,                     -- JSON serialized event
    status VARCHAR(20),               -- PENDING, PUBLISHED, FAILED
    created_at TIMESTAMP,
    published_at TIMESTAMP
);
```

### Step 2: Atomic Database Writes

```java
@Transactional  // ← CRITICAL: both operations in same TX
public void checkout(String paymentMethod) {
    // Create business entity
    Order order = Order.builder()
        .id(UUID.randomUUID().toString())
        .status(PENDING)
        .build();
    orderRepository.save(order);  // ← saves to orders table
    
    // Create event in OUTBOX (same TX)
    OrderCreatedEvent event = buildEvent(order);
    OutboxEvent outboxEvent = OutboxEvent.builder()
        .id(UUID.randomUUID().toString())
        .aggregateType("Order")
        .aggregateId(order.getId())
        .eventType("order.created")
        .payload(objectMapper.writeValueAsString(event))
        .status(PENDING)
        .createdAt(now())
        .build();
    outboxEventRepository.save(outboxEvent);  // ← saves to outbox_events table
    
    // ← Transaction commits – BOTH saved atomically!
}
```

If exception throws:
- Order NOT saved
- OutboxEvent NOT saved
- Clean slate!

### Step 3: Async Publication

```java
@Component
public class OutboxPublisher {
    
    @Scheduled(fixedDelay = 5000)  // Run every 5 seconds
    @Transactional
    public void publishPendingEvents() {
        // Find all PENDING events
        List<OutboxEvent> pending = outboxEventRepository
            .findByStatus(OutboxStatus.PENDING);
        
        for (OutboxEvent event : pending) {
            try {
                // Publish to Kafka
                kafkaTemplate.send(
                    event.getEventType(),           // Topic: "order.created"
                    event.getAggregateId(),         // Key: order ID
                    event.getPayload()              // Value: JSON event
                );
                
                // Mark as published
                event.setStatus(PUBLISHED);
                event.setPublishedAt(now());
                outboxEventRepository.save(event);
                
                log.info("Published event type={}", event.getEventType());
            } catch (Exception e) {
                // If Kafka fails, mark as FAILED, retry later
                event.setStatus(FAILED);
                outboxEventRepository.save(event);
                log.error("Failed to publish event: {}", event.getId(), e);
            }
        }
    }
}
```

---

## Outbox States

```
┌──────────────┐
│   PENDING    │  Event created, waiting to publish
├──────────────┤
│      ↓ (OutboxPublisher runs)
│   PUBLISHED  │  Successfully published to Kafka (idempotent key set)
├──────────────┤
│   FAILED     │  Attempted but failed (manual investigation needed)
└──────────────┘
```

---

## Recovery Guarantees

### Scenario: Kafka Temporarily Down

```
Attempt 1: OutboxPublisher tries to publish → Kafka offline → FAILS
Event stays PENDING in outbox_events table

Attempt 2 (5s later): OutboxPublisher tries again → Kafka back up → SUCCESS
Event marked PUBLISHED
```

Users never see the failure! Event eventually delivered.

### Scenario: Duplicate Events (Kafka at-least-once)

```
OutboxPublisher publishes event
    ↓
Kafka: "I received event {id: 123, orderId: abc}"
    ↓
OutboxPublisher tries to mark as PUBLISHED
    ↓
Network fails before update!

OutboxPublisher later tries again:
    ↓
Publishes same event to Kafka AGAIN (duplicate!)

Kafka: at-least-once delivery
Services must be idempotent!
```

PaymentService must check:
```java
if (paymentRepository.findByOrderId(orderEvent.getOrderId()).isPresent()) {
    return;  // Already processed, skip
}
```

---

## Outbox in Our Project

### order-service/model/OutboxEvent.java

```java
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
    @Id
    private String id;
    
    private String aggregateType;    // "Order"
    private String aggregateId;      // order ID
    private String eventType;        // Kafka topic: "order.created"
    
    @Column(columnDefinition = "TEXT")
    private String payload;          // JSON-serialised OrderCreatedEvent
    
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;     // PENDING, PUBLISHED, FAILED
    
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
}
```

### order-service/outbox/OutboxPublisher.java

```java
@Component
@Slf4j
public class OutboxPublisher {
    
    @Autowired
    private OutboxEventRepository outboxEventRepository;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Scheduled(fixedDelayString = "${outbox.publisher.delay-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository
            .findByStatus(OutboxEvent.OutboxStatus.PENDING);
        
        if (pending.isEmpty()) return;
        
        log.debug("Outbox: found {} PENDING event(s)", pending.size());
        
        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send(
                    event.getEventType(),
                    event.getAggregateId(),
                    event.getPayload()
                );
                
                event.setStatus(OutboxEvent.OutboxStatus.PUBLISHED);
                event.setPublishedAt(LocalDateTime.now());
                outboxEventRepository.save(event);
                
                log.info("Published outbox event type={} id={}",
                    event.getEventType(), event.getId());
                
            } catch (Exception e) {
                log.error("Failed to publish outbox event: {}", event.getId(), e);
                event.setStatus(OutboxEvent.OutboxStatus.FAILED);
                outboxEventRepository.save(event);
            }
        }
    }
}
```

### OrderService – Atomic Save

```java
@Transactional  // ← CRITICAL
public OrderResponse checkout(String paymentMethod) {
    // ... validate ...
    
    Order order = Order.builder()
        .id(UUID.randomUUID().toString())
        .status(OrderStatus.PENDING)
        .totalAmount(cartTotal)
        .build();
    
    // Add items
    order.getItems().addAll(orderItems);
    
    // Save order
    orderRepository.save(order);  // ← saves to orders table
    
    // Create & save event in SAME transaction
    OrderCreatedEvent event = buildOrderCreatedEvent(order);
    saveOutboxEvent(order.getId(), event);  // ← saves to outbox_events
    
    // ← Transaction commits – BOTH tables updated!
    
    return toResponse(order, "Order placed, processing payment...");
}

private void saveOutboxEvent(String orderId, OrderCreatedEvent event) {
    try {
        OutboxEvent outboxEvent = OutboxEvent.builder()
            .id(UUID.randomUUID().toString())
            .aggregateType("Order")
            .aggregateId(orderId)
            .eventType(KafkaTopics.ORDER_CREATED)
            .payload(objectMapper.writeValueAsString(event))
            .status(OutboxEvent.OutboxStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();
        outboxEventRepository.save(outboxEvent);
    } catch (Exception e) {
        log.error("Failed to create outbox event", e);
        throw new RuntimeException("Failed to create outbox event", e);
    }
}
```

---

## Advantages

✅ **Atomicity** – At most once creation, at least once publication  
✅ **No Two-Phase Commit** – Single DB transaction  
✅ **Resilient to Kafka outages** – Events queue up in local DB  
✅ **Auditable** – Full event log in outbox table  
✅ **Simple** – Just polling + publishing  

---

## Disadvantages

❌ **Extra table** – Adds operational complexity  
❌ **Polling latency** – Events published after delay (configurable 5s)  
❌ **Manual cleanup** – Old published events must be archived/deleted  

---

## Alternatives

### 1. **Change Data Capture (CDC)**

Automatically detect DB changes and publish events.

```
┌──────────────────────┐
│ Database             │
│ (orders table)       │
└──────────────────────┘
        │ (changes)
        ▼ CDC Tool (Debezium, Maxwell)
    publishes to Kafka
```

More complex but automatic!

### 2. **Event Sourcing**

Store events as primary source of truth (not entities).

```
Instead of: users table, orders table, ...
Store: sequence of events (append-only log)

User created
→ Item added to cart
→ Order placed
→ Payment processed
```

More powerful but much more complex!

---

## Real-World Analogy

Transactional Outbox is like a **postal system**:

- You write a letter (create event)
- You put it in outbox on your desk (save to outbox table)
- Mailman comes periodically (scheduler)
- Mailman picks up letters (polls pending events)
- Mailman delivers to post office (publishes to Kafka)
- You get receipt (mark published)

All coordination handled by postal system, not you!

---

## Further Reading

- **Chris Richardson - Transactional Outbox**: https://microservices.io/patterns/data/transactional-outbox.html
- **Debezium CDC**: https://debezium.io/

---

**Next**: Learn about resilience patterns → [Circuit Breaker Pattern](./09-circuit-breaker-pattern.md)

