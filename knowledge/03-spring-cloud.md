# Spring Cloud

## What is Spring Cloud?

**Spring Cloud** is a **suite of tools** built on top of Spring Boot for building **distributed systems** and **microservices**. It solves common challenges:

- **Service discovery** – How do microservices find each other?
- **Load balancing** – Route requests across multiple instances
- **Configuration management** – Centralized config for multiple services
- **API Gateway** – Single entry point for all requests
- **Circuit breakers** – Prevent cascading failures
- **Distributed tracing** – Track requests across services

```
Spring Boot (single application)
    ↓
Spring Cloud (distributed system of applications)
    ├── Service A
    ├── Service B
    └── Service C
```

---

## Spring Cloud Components in Our Project

### 1. **Spring Cloud Netflix Eureka** (Service Discovery)

**What**: Every service registers itself and discovers others via a central registry.

```
┌──────────────────────────┐
│   Eureka Server (8761)   │
│   Registry of services   │
└──────────────────────────┘
    ↑        ↑        ↑
    │        │        │
Product   Cart   Order
Service   Service Service
(registers itself)
```

**In application.yml**:
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

Spring Cloud automatically registers your service on startup and fetches the registry periodically.

### 2. **Spring Cloud Gateway** (API Gateway)

**What**: Single entry point that routes requests to appropriate microservices.

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: product-service
          uri: lb://product-service  # Load-balanced via Eureka
          predicates:
            - Path=/api/products/**
          filters:
            - name: CircuitBreaker
              args:
                name: productCircuitBreaker
```

Gateway automatically **discovers** `product-service` via Eureka, applies **circuit breakers**, and **routes** requests.

### 3. **Spring Cloud OpenFeign** (Synchronous Calls)

**What**: Declarative HTTP client for calling other services.

**Old way** (verbose):
```java
@Service
public class CartService {
    @Autowired
    private RestTemplate restTemplate;
    
    public Product getProduct(int id) {
        String url = "http://product-service/api/products/" + id;
        return restTemplate.getForObject(url, Product.class);
    }
}
```

**With Feign** (clean):
```java
@FeignClient(name = "product-service")
public interface ProductServiceClient {
    @GetMapping("/api/products/{id}")
    Product getProduct(@PathVariable int id);
}

@Service
public class CartService {
    @Autowired
    private ProductServiceClient productClient;
    
    public Product getProduct(int id) {
        return productClient.getProduct(id);  // One line!
    }
}
```

Feign automatically:
- Discovers `product-service` via Eureka
- Loads balances requests across instances
- Applies circuit breakers
- Serializes/deserializes JSON

### 4. **Spring Cloud LoadBalancer**

**What**: Distributes requests across multiple instances of the same service.

```
RequestsRequest 1 ──┐
Request 2 ──┤
Request 3 ──┤     ┌─────────────────────┐
Request 4 ──┤     │  Load Balancer      │
Request 5 ──┤     │ (Round Robin)       │
            └─→───┤                     │
                  │  ┌───────────────┐  │
                  ├─→│ Instance 1    │  │
                  │  ├───────────────┤  │
                  ├─→│ Instance 2    │  │
                  │  ├───────────────┤  │
                  └─→│ Instance 3    │  │
                     └───────────────┘  │
                  └─────────────────────┘
```

When you use `lb://product-service`, Spring Cloud LoadBalancer:
1. Queries Eureka for all `product-service` instances
2. Picks one (round-robin, random, weighted)
3. Routes the request there
4. Handles failures by trying another instance

---

## Spring Cloud Diagram

```
┌──────────────────────────────────────────────────┐
│             Spring Cloud Architecture            │
├──────────────────────────────────────────────────┤
│                                                  │
│  Browser                                         │
│     │                                            │
│     ▼                                            │
│  API Gateway (Spring Cloud Gateway)              │
│  ├── Eureka-aware routing                       │
│  ├── Circuit breakers (Resilience4j)            │
│  └── Load balancing                             │
│     │                                            │
│     ├──────────────────────────┬──────────────┐  │
│     ▼                          ▼              ▼  │
│  Service A               Service B    Service C  │
│  ├─ Eureka registration   │                 │   │
│  ├─ Feign client          │                 │   │
│  └─ LoadBalancer          │                 │   │
│                           │                 │   │
│  ┌──────────────────────────────────────────┐  │
│  │       Eureka Service Registry (8761)      │  │
│  │                                           │   │
│  │  Service A: [localhost:8001, ...]        │   │
│  │  Service B: [localhost:8002, ...]        │   │
│  │  Service C: [localhost:8003, ...]        │   │
│  └──────────────────────────────────────────┘  │
│                                                 │
└──────────────────────────────────────────────────┘
```

---

## Spring Cloud Config Server (Not Used Here)

Centralizes configuration for all services:

```
Config Server (git repository)
    │
    ├── service1/application.yml
    ├── service2/application.yml
    └── service3/application.yml
```

Services fetch config on startup. Perfect for managing config across environments (dev, staging, prod).

---

## Spring Cloud Circuit Breaker (Resilience4j)

**What**: Prevents cascading failures by stopping requests to failing services.

```java
@FeignClient(
    name = "product-service",
    fallback = ProductServiceClientFallback.class  // fallback implementation
)
public interface ProductServiceClient {
    @GetMapping("/api/products/{id}")
    Product getProduct(@PathVariable int id);
}

@Component
public class ProductServiceClientFallback implements ProductServiceClient {
    @Override
    public Product getProduct(int id) {
        // Return placeholder when product-service is down
        return Product.builder()
            .id(id)
            .name("Product Unavailable")
            .build();
    }
}
```

When `product-service` is down:
1. First few requests fail
2. Circuit opens (OPEN state)
3. Further requests immediately return fallback
4. After timeout, circuit tries one request (HALF_OPEN)
5. If successful, circuit closes (CLOSED)

---

## In Our Project

### service-registry Application

```java
@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
```

### Every Microservice

```yaml
spring:
  application:
    name: product-service

eureka:  # Register with Eureka Server
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### API Gateway

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: product-service
          uri: lb://product-service       # Load-balanced Eureka lookup
          predicates:
            - Path=/api/products/**
          filters:
            - name: CircuitBreaker         # Circuit breaker protection
              args:
                name: productCircuitBreaker
                fallbackUri: forward:/fallback/product
```

### Feign Client (CartService → ProductService)

```java
@FeignClient(
    name = "product-service",
    fallback = ProductServiceClientFallback.class
)
public interface ProductServiceClient {
    @GetMapping("/api/products/{id}")
    ResponseEntity<ProductDto> getProduct(@PathVariable Integer id);
}
```

Then:
```java
@Service
public class CartService {
    @Autowired
    private ProductServiceClient productServiceClient;
    
    public CartResponse addToCart(Integer productId) {
        ResponseEntity<ProductDto> response = productServiceClient.getProduct(productId);
        // CartService automatically discovered product-service via Eureka!
    }
}
```

---

## Request Flow Example

```
1. Browser: GET http://localhost:8090/
   └─> Frontend Service (port 8090)

2. Frontend JS: fetch('http://localhost:8080/api/cart')
   └─> API Gateway (port 8080, all requests go through here)

3. API Gateway receives /api/cart request
   └─> Looks up "cart-service" in Eureka registry
   └─> Finds: cart-service at 127.0.0.1:8082
   └─> Routes to: http://127.0.0.1:8082/api/cart

4. CartService receives request
   └─> Needs product details
   └─> Feign client calls: productServiceClient.getProduct(1)
   └─> LoadBalancer discovers product-service in Eureka
   └─> Finds: product-service at 127.0.0.1:8081
   └─> HTTP GET to: http://127.0.0.1:8081/api/products/1

5. ProductService responds with product JSON
   └─> Feign deserializes to ProductDto
   └─> CartService builds cart response

6. CartService response → API Gateway → Browser
```

---

## Common Spring Cloud Patterns

| Pattern | Purpose | Component |
|---------|---------|-----------|
| Service Discovery | Find services | Eureka |
| Client-side Load Balancing | Distribute load | LoadBalancer |
| Server-side Load Balancing | Route & balance | Gateway |
| Circuit Breaker | Prevent cascades | Resilience4j |
| Retry | Implement retries | Spring Retry |
| Configuration Management | Centralize config | Config Server |
| Distributed Tracing | Track requests | Sleuth + Zipkin |
| API Gateway | Single entry point | Gateway |

---

## Comparison with Other Frameworks

| Aspect | Spring Cloud | AWS Microservices | Kubernetes |
|--------|-------------|-------------------|------------|
| Service Discovery | Eureka | AWS Service Discovery | Built-in (DNS) |
| Load Balancing | LoadBalancer + Gateway | ELB / ALB | Service object |
| Configuration | Spring Cloud Config | Parameter Store | ConfigMaps |
| Learning Curve | Moderate | Steep (AWS-specific) | Steep (Kubernetes) |
| Vendor Lock-in | No | Yes (AWS) | No |

---

## Further Reading

- **Spring Cloud Docs**: https://spring.io/projects/spring-cloud
- **Eureka**: https://github.com/Netflix/eureka/wiki
- **OpenFeign**: https://spring.io/projects/spring-cloud-openfeign
- **Spring Cloud Gateway**: https://spring.io/projects/spring-cloud-gateway

---

**Next**: Dive deeper into Microservices Fundamentals → [Microservices Fundamentals](./04-microservices-fundamentals.md)

