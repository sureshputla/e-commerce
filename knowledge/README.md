# Knowledge Base - E-Commerce Microservices Learning Guide

Welcome to the comprehensive learning guide for this e-commerce microservices platform. This folder contains detailed explanations of every technology, pattern, and methodology used in the project.

## 📑 Index

### Core Technologies
1. **[Java 17 & JVM](./01-java-17-jvm.md)** – The runtime environment
2. **[Spring Boot 3](./02-spring-boot-3.md)** – Application framework
3. **[Spring Cloud](./03-spring-cloud.md)** – Distributed systems toolkit

### Microservices Architecture
4. **[Microservices Fundamentals](./04-microservices-fundamentals.md)** – Concepts and principles
5. **[Service Discovery (Eureka)](./05-service-discovery-eureka.md)** – How services find each other
6. **[API Gateway Pattern](./06-api-gateway-pattern.md)** – Single entry point

### Distributed Patterns
7. **[Choreography Saga Pattern](./07-choreography-saga.md)** – Distributed transactions
8. **[Transactional Outbox Pattern](./08-transactional-outbox.md)** – Reliable messaging
9. **[Circuit Breaker Pattern](./09-circuit-breaker-pattern.md)** – Fault tolerance

### Inter-Service Communication
10. **[OpenFeign & Synchronous Calls](./10-openfeign-sync-calls.md)** – Request-response
11. **[Apache Kafka & Event-Driven Architecture](./11-kafka-event-driven.md)** – Async messaging

### Data & Persistence
12. **[Spring Data JPA & Hibernate](./12-spring-data-jpa-hibernate.md)** – Object-Relational Mapping
13. **[H2 Database](./13-h2-database.md)** – In-memory database for dev/test

### Code Quality & Productivity
14. **[Lombok](./14-lombok.md)** – Boilerplate elimination
15. **[MapStruct](./15-mapstruct.md)** – Zero-reflection object mapping

### API & Monitoring
16. **[RESTful APIs with Spring Web](./16-rest-apis-spring-web.md)** – HTTP endpoints
17. **[Springdoc OpenAPI & Swagger](./17-springdoc-openapi-swagger.md)** – API documentation
18. **[Spring Boot Actuator](./18-spring-boot-actuator.md)** – Monitoring & health checks

### Resilience & Fault Tolerance
19. **[Resilience4j](./19-resilience4j.md)** – Advanced circuit breaker & retry
20. **[LoadBalancer & Service Mesh Concepts](./20-load-balancer-concepts.md)** – Traffic distribution

### Build & DevOps
21. **[Apache Maven](./21-apache-maven.md)** – Build tool & dependency management
22. **[Docker & Docker Compose](./22-docker-docker-compose.md)** – Containerization

### Frontend & UI
23. **[Thymeleaf](./23-thymeleaf.md)** – Server-side templating
24. **[Vanilla JavaScript & Async/Await](./24-vanilla-javascript-async.md)** – Frontend interactions

### Methodologies
25. **[Domain-Driven Design (DDD)](./25-domain-driven-design.md)** – Architecture principles
26. **[Event-Driven Architecture](./26-event-driven-architecture.md)** – Design methodology
27. **[Continuous Integration & Testing](./27-ci-testing.md)** – Quality practices

---

## How to Use This Knowledge Base

1. **Start with Core Concepts**: Begin with Java & Spring Boot if you're new to Java
2. **Understand Architecture**: Learn microservices fundamentals before diving into patterns
3. **Study Patterns**: Deep dive into Saga, Outbox, and Circuit Breaker
4. **Practical Application**: See how each technology is used in the project
5. **Cross-Reference**: Each file links to related concepts

---

## Quick Technology Map

```
Your Browser
    ↓
Thymeleaf UI (23) + Vanilla JS (24)
    ↓ HTTP
Spring Boot (02) + Spring Web (16)
    ↓ WEB FRAMEWORK
API Gateway (06)
    ├── circuits: Resilience4j (19)
    └── routes via Spring Cloud (03)
    ↓ ROUTING
Microservices (04) using:
├── Spring Boot (02)
├── Spring Data JPA (12) + Hibernate (12)
├── H2 Database (13)
├── Lombok (14)
└── MapStruct (15)
    ↓ SYNC CALLS
OpenFeign (10)
    ├── discovers via Eureka (05)
    └── protected by Circuit Breaker (09)
    ↓ ASYNC EVENTS
Kafka (11)
    ├── events stored via Outbox (08)
    ├── choreography via Saga (07)
    └── consumed by other services
    ↓ MONITORING
Actuator (18) + OpenAPI (17)

BUILD: Maven (21) → Docker (22)
```

---

## Learning Path Recommendation

### For Beginners (Week 1-2)
```
Java 17 → Spring Boot → REST APIs → Microservices Fundamentals
```

### For Intermediate (Week 3-4)
```
Service Discovery → API Gateway → Feign Clients → Basic Kafka
```

### For Advanced (Week 5-6)
```
Choreography Saga → Transactional Outbox → Circuit Breakers → Event-Driven Architecture
```

---

## Each File Contains

- **What is it?** – Definition and purpose
- **Why use it?** – Benefits and use cases
- **How does it work?** – Internal mechanics
- **In our project** – Specific usage in this codebase
- **Code examples** – Real examples from the project
- **Comparison** – Alternatives and trade-offs
- **Real-world analogy** – Easy-to-understand explanation
- **Further reading** – Links and resources

---

**Happy Learning!** 🚀

Start with the file that interests you most, or follow the recommended learning path. Each file is self-contained but cross-references others for deeper understanding.

