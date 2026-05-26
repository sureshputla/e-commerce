# Apache Kafka & Event-Driven Architecture

## What is Apache Kafka?

**Kafka** is a distributed **event streaming platform** – think of it as a **message bus** or **event log**. Services publish events to Kafka topics, and other services subscribe and react.

```
Event Producers                Event Log (Kafka)              Event Consumers
    │                                  │                            │
    ├─ OrderService ──────┐           │                   ┌────→ PaymentService
    │                      ├─→ "order.created" topic ─────┤
    ├─ PaymentService ────┤            │                   ├────→ CartService
    │                      │            │                   ├────→ NotificationService
    └─ Any service ───────┘            │                   └────→ Any subscriber
```

### Key Concepts

**Topic**: A named event stream
```
"order.created"        – Order events
"payment.processed"    – Payment events
"user.registered"      – User events
```

**Producer**: Publishes events to a topic
```java
kafkaTemplate.send("order.created", orderEvent);
```

**Consumer**: Subscribes to a topic, processes events
```java
@KafkaListener(topics = "order.created")
public void onOrderCreated(String event) { ... }
```

**Partition**: Topic divided into ordered queues
```
Topic: order.created
├── Partition 0: [Event1, Event2, Event3, ...]
├── Partition 1: [Event4, Event5, Event6, ...]
└── Partition 2: [Event7, Event8, Event9, ...]

Multiple consumers can read in parallel!
Each consumer reads from one or more partitions.
```

---

## Pull vs Push

### Push Model (Traditional - RabbitMQ)
```
Broker pushes messages to consumer
    │
    ├─→ Consumer 1
    ├─→ Consumer 2
    └─→ Consumer 3

Problem: Broker doesn't know consumer's capacity
Can overwhelm a slow consumer
```

### Pull Model (Kafka - Better)
```
Consumer 1: "Give me events"
Consumer 2: "Give me events"
Consumer 3: "Give me events"

Broker: "OK, here are events"

Each consumer pulls at its own pace
No backpressure issues!
```

---

## Durability & Persistence

Unlike RabbitMQ (in-memory), Kafka **persists all events to disk**.

```
┌─────────────────────────┐
│ Kafka Broker            │
├─────────────────────────┤
│ Topic: order.created    │
│                         │
│ Partition 0:            │
│ ┌─────────────────┐    │
│ │ [Event log...]  │ ←─ Disk
│ └─────────────────┘    │
└─────────────────────────┘

Events persisted even if broker crashes!
```

**Retention Policy**:
```
Keep events for 7 days
OR
Keep last 1 GB of events
OR
Keep indefinitely

Consumer can replay from any point!
```

---

## At-Least-Once Delivery

Kafka guarantees **at-least-once delivery**:

```
Event published once
    ↓
Kafka: "Got it, stored to disk"
    ↓
Consumer receives event
    ↓
Consumer processes
    ↓
Consumer crashes before sending offset commit

Kafka thinks: "Consumer never got it"
    ↓
Redelivers same event

Result: DUPLICATE EVENT!
```

**Solution**: Make consumers **idempotent**

```java
@KafkaListener(topics = "order.created")
public void onOrderCreated(OrderCreatedEvent event) {
    // Check if already processed
    if (paymentRepository.findByOrderId(event.getOrderId()).isPresent()) {
        log.warn("Already processed, skipping");
        return;
    }
    
    // Process
    processPayment(event);
}
```

---

## Consumer Groups

Multiple consumers can read the same topic via **consumer groups**.

```
Topic: order.created
├─ Partition 0
├─ Partition 1
└─ Partition 2

Consumer Group 1 (Email Service):
├─ Consumer 1a reads Partition 0
├─ Consumer 1b reads Partition 1
└─ Consumer 1c reads Partition 2

Consumer Group 2 (Analytics Service):
├─ Consumer 2a reads Partition 0, 1, 2 (sequential)

Different groups read independently!
Each group maintains its own position (offset).
```

---

## In Our Project

### Architecture

```yaml
docker-compose.yml
├── Zookeeper (coordination)
├── Kafka (message broker)
└── Kafka UI (web UI for inspection)
```

### Topics Used

```
order.created
    └── Published by: OrderService
    └── Consumed by: PaymentService, CartService

payment.processed
    └── Published by: PaymentService
    └── Consumed by: OrderService, NotificationService
```

### Configuration (Spring Kafka)

```yaml
# application.yml (OrderService)
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all                  # Wait for all replicas
      retries: 3                 # Retry 3 times on failure
    consumer:
      group-id: order-service    # Consumer group
      auto-offset-reset: earliest # Start from beginning if no offset
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

### Publishing (OutboxPublisher)

```java
@Component
public class OutboxPublisher {
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    public void publishEvent(OutboxEvent event) {
        try {
            kafkaTemplate.send(
                event.getEventType(),      // Topic: "order.created"
                event.getAggregateId(),    // Key: order ID (for partitioning)
                event.getPayload()         // Value: JSON event
            );
            log.info("Published to Kafka");
        } catch (Exception e) {
            log.error("Failed to publish", e);
            throw new RuntimeException(e);
        }
    }
}
```

### Consuming (PaymentService)

```java
@Component
@Slf4j
public class OrderEventListener {
    
    @Autowired
    private PaymentService paymentService;
    
    @KafkaListener(topics = "order.created", groupId = "payment-service")
    public void onOrderCreated(String payload) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            log.info("Received order.created event: {}", event.getOrderId());
            
            // Process payment
            paymentService.processPayment(event);
        } catch (Exception e) {
            log.error("Failed to process event", e);
            // Don't re-throw – Kafka will retry
        }
    }
}
```

---

## Event-Driven Architecture

**Event-Driven** means services communicate **asynchronously via events**, not synchronous API calls.

### Synchronous (Traditional)

```
OrderService ──HTTP request──→ PaymentService
    ↑                              │
    └──────HTTP response───────────┘

OrderService: "Process payment"
PaymentService: "OK, here's result"

Waits for response!
If PaymentService is slow/down ⇒ OrderService blocked
```

### Asynchronous (Event-Driven)

```
OrderService ──publish──→ Kafka Topic "order.created"
                              ↓
                     PaymentService (subscribes)
                     ├─ receives event
                     ├─ processes independently
                     └─ publishes result

OrderService: doesn't wait!
ParallelProcessing!
Decoupled!
```

### Flow in Our Project

```
1. OrderService publishes "order.created" event
   ↓ (non-blocking, returns immediately)
2. PaymentService consumes "order.created"
   ├─ Processes payment in background
   ├─ Publishes "payment.processed"
   └─ OrderService consumes to update status
3. CartService consumes "order.created"
   └─ Clears cart in background
4. NotificationService consumes "payment.processed"
   └─ Sends notification in background
```

All happening **asynchronously**, **independently**!

---

## Kafka Guarantees

| Guarantee | What It Means |
|-----------|--------------|
| **Ordering** | Events in a partition stay ordered (FIFO) |
| **Durability** | Events persisted to disk, survive crashes |
| **At-Least-Once** | Event delivered at least once (may duplicate) |
| **No Exactly-Once** | ⚠️ Hard to guarantee, apps must be idempotent |

---

## Monitoring Kafka (Kafka UI)

```
http://localhost:8989
```

Shows:
- All topics
- Messages in each topic
- Consumer groups
- Lag (how far behind)
- Performance metrics

Perfect for debugging!

---

## Kafka vs RabbitMQ vs SNS/SQS

| Aspect | Kafka | RabbitMQ | AWS SNS/SQS |
|--------|-------|----------|------------|
| **Model** | Pub/Sub + Log | Pub/Sub + Queue | Pub/Sub + Queue |
| **Persistence** | ✓ Disk-based | ✗ Memory (optional) | ✓ Cloud storage |
| **Replay** | ✓ Can replay | ✗ No replay | ✗ No replay |
| **Scale** | ✓✓✓ Massive | ✓ Good | ✓✓ VPC limited |
| **Latency** | 10ms+ | <1ms | 100ms+ |
| **Learning** | Steep | Moderate | Easy (managed) |
| **Cost** | Self-hosted | Self-hosted | Managed (pay) |

**Our choice**: Kafka – great for event streaming, replay capability.

---

## Real-World Analogy

Kafka is like a **newspaper printing press**:

- **Publishers (Services)**: Write articles (events)
- **Kafka**: Prints and distributes newspapers (persists events)
- **Subscribers (Services)**: Read newspapers independently (consume events)
- **Topics**: Different sections (News, Sports, Weather)
- **Partitions**: Multiple printing lines (parallelism)
- **Consumer Groups**: Different reader groups (email, analytics, notifications)

Each reader group gets its own copy!
Readers can read at their own pace!

---

## Further Reading

- **Kafka Official**: https://kafka.apache.org/intro
- **Spring Kafka**: https://spring.io/projects/spring-kafka
- **Confluent Kafka Guide**: https://developer.confluent.io/quickstart/

---

**Next**: Learn about boilerplate reduction → [Lombok](./14-lombok.md)

