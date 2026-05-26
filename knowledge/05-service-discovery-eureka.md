# Service Discovery (Eureka)

## What is Service Discovery?

In a microservices system, services are **dynamically deployed** on different hosts and ports. Service Discovery solves:

**The Problem**: How does Service A find Service B's address if we don't know where it's running?

```
Before:
OrderService calls CartService at: http://192.168.1.100:8082

But what if CartService:
- Moved to 192.168.1.101?
- Scaled to 3 instances (ports 8082, 8083, 8084)?
- Crashed and restarted on a different port?

Hardcoding addresses is FRAGILE!
```

---

## Eureka Solution

**Eureka** is Netflix's **Service Registry**. Services self-register, and clients query the registry.

```
1. Service starts
2. Service calls Eureka: "I'm cart-service at localhost:8082"
3. Eureka stores: cart-service → localhost:8082
4. OrderService calls Eureka: "Where is cart-service?"
5. Eureka returns: localhost:8082
6. OrderService calls: http://localhost:8082/api/cart
```

### Eureka Architecture

```
┌─────────────────────────────────────┐
│     Eureka Server (Registry)        │
│     (single source of truth)        │
├─────────────────────────────────────┤
│ Registered Services:                │
│  cart-service:   [localhost:8082]   │
│  order-service:  [localhost:8084]   │
│  payment-service:[localhost:8085]   │
│  product-service:[localhost:8081]   │
└─────────────────────────────────────┘
           ↓        ↑        ↓
      (register) (query) (heartbeat)
           │        │        │
      ┌────┴────┬──┴────┬────┴────┐
      ▼         ▼       ▼         ▼
 CartSvc   OrderSvc PaymentSvc ProductSvc
```

---

## Eureka Lifecycle

### 1. **Service Startup**

```
CartServiceApplication starts
   ↓
ClassLoader finds: @EnableEurekaClient
   ↓
Spring Cloud Netflix registers self with Eureka
   ↓
Makes HTTP POST to: http://localhost:8761/eureka/apps/CART-SERVICE
   ├── hostname: localhost
   ├── port: 8082
   ├── status: UP
   └── instanceId: localhost:CART-SERVICE:8082
   ↓
Eureka stores registration
   ↓
Service ready to receive requests ✓
```

### 2. **Client Discovery**

```
OrderService needs to call CartService
   ↓
OrderService queries Eureka:
   GET http://localhost:8761/eureka/apps/CART-SERVICE
   ↓
Eureka returns list of instances:
   [
     { hostname: localhost, port: 8082 },
     { hostname: localhost, port: 8083 },
     { hostname: localhost, port: 8084 }
   ]
   ↓
LoadBalancer picks one (round-robin)
   ↓
OrderService calls: http://localhost:8082/api/cart
```

### 3. **Heartbeat & Health**

```
CartService sends heartbeat every 30 seconds:
   PUT http://localhost:8761/eureka/apps/CART-SERVICE/localhost:CART-SERVICE:8082
   ↓
Eureka says: "OK, still consider it UP"

If CartService crashes:
   No heartbeat for 90+ seconds
   ↓
Eureka marks it as DOWN
   ↓
New instances won't route to it
```

---

## Self-Registration vs Client-Side Discovery

### Self-Registration (What We Use)

```java
// Service registers itself
@SpringBootApplication
@EnableEurekaClient  // Spring Cloud Netflix
public class CartServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }
}

// application.yml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

Service **automatically regist itself** with Eureka.

### Third-Party Registration

An external system (e.g., deployment agent) registers services. Services don't know about Eureka.

**Pros**: Service code cleaner, Services platform-agnostic  
**Cons**: External orchestration complexity

---

## Client-Side Service Discovery

How does Order Service find CartService?

### Option 1: Direct Eureka Query (Bad)

```java
@Service
public class OrderService {
    @Autowired
    private DiscoveryClient discoveryClient;
    
    public Cart getCart() {
        List<ServiceInstance> instances = discoveryClient.getInstances("CART-SERVICE");
        // Pick one, make HTTP call, handle failures...
        // Lots of boilerplate!
    }
}
```

### Option 2: Feign (Best - Automatic)

```java
@FeignClient(name = "cart-service")  // Eureka lookup happens automatically!
public interface CartServiceClient {
    @GetMapping("/api/cart")
    CartResponse getCart();
}

@Service
public class OrderService {
    @Autowired
    CartServiceClient cartClient;
    
    public Cart getCart() {
        return cartClient.getCart();  // One line! Eureka discovery + load balancing handled!
    }
}
```

Feign automatically:
1. Queries Eureka for `cart-service`
2. Load balances across instances
3. Retries if needed
4. Applies circuit breakers

---

## In Our Project

### service-registry Module

```java
@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
```

**configuration**:
```yaml
# service-registry/application.yml
eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false    # ITSELF doesn't register
    fetch-registry: false          # ITSELF doesn't fetch
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Each Microservice

```yaml
# product-service/application.yml
spring:
  application:
    name: product-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

### Eureka Dashboard

```
http://localhost:8761
```

Shows all registered services in real-time!

```
Application    | Instances                | Status
────────────────┼──────────────────────────┼─────────
PRODUCT-SERVICE | localhost:PRODUCT-SERVICE:8081 | UP
CART-SERVICE    | localhost:CART-SERVICE:8082    | UP
ORDER-SERVICE   | localhost:ORDER-SERVICE:8084   | UP
```

---

## Service Instance Data

When a service registers, Eureka stores:

```java
{
  "instanceId": "localhost:CART-SERVICE:8082",
  "hostName": "localhost",
  "app": "CART-SERVICE",
  "ipAddr": "127.0.0.1",
  "port": 8082,
  "status": "UP",
  "metadata": {
    "management.port": "8082"
  },
  "leaseInfo": {
    "renewalIntervalInSecs": 30,
    "durationInSecs": 90
  }
}
```

---

## Zone-Aware Service Discovery

In production (multi-region), Eureka prefers same-zone instances:

```
Region US-EAST-1:
  Zone-1:
    ├── cart-service-1
    ├── product-service-1
    └── order-service-1
  Zone-2:
    ├── cart-service-2
    ├── product-service-2
    └── order-service-2

OrderService in Zone-1 prefers calling CartService in Zone-1
Reduces latency, saves bandwidth!
```

---

## Eureka Consistency Model

Eureka is **eventually consistent**:

```
Service registers
  ↓
Eureka Server updated immediately
  ↓
Other servers in cluster get update (replication)
  ↓
Clients refresh their cache (every 30s)
  ↓
All clients know about new service

During this window: Some clients may not know! (OK, not critical)
```

**Why?** More reliable than strong consistency. Can tolerate Eureka server outages.

---

## Alternatives to Eureka

| Tool | Consistency | Method | Deployment |
|------|-------------|--------|------------|
| **Eureka** | Eventual | Self-register | Netflix |
| **Consul** | Strongly | Self/third-party | HashiCorp |
| **etcd** | Strongly | Third-party | CoreOS |
| **Kubernetes DNS** | Strong | Automatic | Kubernetes |

---

## Real-World Analogy

Eureka is like a **telephone directory**:

- **Businesses (Services)** call directory and register: "I'm Pizza Hut, call me at 555-1234"
- **Customers (Clients)** look up phone number: "What's Pizza Hut's number?" → "555-1234"
- **Directory (Eureka) maintains the list** updated in real-time
- **If Pizza Hut moves**, it updates registry with new number
- **If a customer calls old number**, directory might be stale (eventual consistency)

---

## Further Reading

- **Netflix Eureka**: https://github.com/Netflix/eureka/wiki
- **Spring Cloud Netflix**: https://spring.io/projects/spring-cloud-netflix
- **Chris Richardson - Service Discovery**: https://microservices.io/patterns/service-discovery.html

---

**Next**: Learn how to use Feign clients → [OpenFeign & Synchronous Calls](./10-openfeign-sync-calls.md)  
**Or**: Learn how API Gateway routes → [API Gateway Pattern](./06-api-gateway-pattern.md)

