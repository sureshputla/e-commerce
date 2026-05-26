# Spring Boot 3

## What is Spring Boot?

**Spring Boot** is a **convention-over-configuration framework** that makes it incredibly easy to build production-grade Spring applications. It eliminates boilerplate and gets you up and running in minutes.

### Before Spring Boot (Old Way - Painful)

```xml
<!-- Need 100s of XML lines for configuration -->
<bean id="dataSource" ...>
<bean id="transactionManager" ...>
<bean id="sessionFactory" ...>
<!-- Dependency versions constantly conflict -->
```

### With Spring Boot (Modern Way - Simple)

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

✅ Auto-configured! ✅ Ready to run!

---

## What is Spring 6 (Inside Spring Boot 3)?

Spring Boot 3 runs on **Spring Framework 6**, which brings:

- **Native support** with GraalVM (compile to native executable)
- **Virtual Threads** preview (project Loom)
- **Java records** integration
- **Faster startup** and lower memory footprint

---

## Core Components of Spring Boot

### 1. **Auto-Configuration**

Spring Boot automatically detects and configures components based on what's on the classpath.

```java
// File: application.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: create-drop
```

Spring Boot **auto-detects** H2 on the classpath and **automatically** configures DataSource, JpaRepository, EntityManager, etc.

### 2. **Embedded Server**

No need to deploy to Tomcat! Spring Boot **embeds Tomcat** inside your JAR.

```
application.jar
├── tomcat/           ← embedded
├── classes/
├── lib/
└── MANIFEST.MF
```

### 3. **Starter Dependency POMs**

Instead of manually managing 20 dependency versions:

```xml
<!-- Old way: manage 20 dependencies manually -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <groupId>org.springframework</groupId>
    <artifactId>spring-core</artifactId>
    <!-- ... 18 more ... -->
</dependency>

<!-- New way: one starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

All transitive dependencies are **automatically managed** and **version-compatible**.

### 4. **Actuator**

Built-in monitoring endpoints.

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}

curl http://localhost:8080/actuator/metrics
# Prometheus-compatible metrics
```

---

## Spring Boot Lifecycle

```
1. Application.main() called
    ↓
2. SpringApplication.run() bootstraps
    ↓
3. Environment & Properties loaded (application.yml)
    ↓
4. Spring Context created
    ↓
5. Classpath scanned for @Component, @Service, @Repository
    ↓
6. Beans instantiated & injected (@Autowired)
    ↓
7. Auto-configuration applies (if enabled)
    ↓
8. ApplicationRunner / CommandLineRunner runs (if defined)
    ↓
9. ApplicationReadyEvent fired
    ↓
10. Server starts (Tomcat on port 8080)
    ↓
11. Ready for requests ✅
```

---

## Dependency Injection (DI)

Spring **injects dependencies** automatically. No manual creation!

```java
// Without Spring (manual creation):
class OrderService {
    private CartService cartService = new CartService();
    private PaymentService paymentService = new PaymentService();
    // Hard-coded, hard to test!
}

// With Spring (automatic injection):
@Service
class OrderService {
    private final CartService cartService;
    private final PaymentService paymentService;
    
    // Spring injects these automatically!
    public OrderService(CartService cartService, PaymentService paymentService) {
        this.cartService = cartService;
        this.paymentService = paymentService;
    }
}
```

### Injection Methods

```java
// 1. Constructor injection (BEST - immutable)
@Service
public class ProductService {
    private final ProductRepository repository;
    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }
}

// 2. Field injection (NOT RECOMMENDED - can be null)
@Service
public class ProductService {
    @Autowired
    private ProductRepository repository;
}

// 3. Setter injection (RARELY USED)
@Service
public class ProductService {
    private ProductRepository repository;
    @Autowired
    public void setRepository(ProductRepository repository) {
        this.repository = repository;
    }
}
```

**Why constructor injection?** Immutable, testable, enforces required dependencies.

---

## Common Annotations

| Annotation | Purpose |
|-----------|---------|
| `@SpringBootApplication` | Main application class |
| `@Bean` | Register method's return as a Spring bean |
| `@Component` | Generic Spring-managed component |
| `@Service` | Business logic component |
| `@Repository` | Data access component |
| `@Controller` / `@RestController` | Web endpoint handlers |
| `@Autowired` | Inject dependency |
| `@Value` | Inject property value |
| `@ConfigurationProperties` | Bind YAML to Java objects |
| `@Transactional` | Declarative transaction management |
| `@Scheduled` | Run method periodically |
| `@Async` | Run method asynchronously |

---

## In Our Project

### ProductServiceApplication.java

```java
@SpringBootApplication
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
```

### Auto-Configuration in Action

```yaml
# product-service/application.yml
spring:
  datasource:
    url: jdbc:h2:mem:productdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create
```

Spring Boot sees H2 on classpath → auto-configures DataSource, JPA, Hibernate. You write **0 lines** of Java configuration!

### Dependency Injection in Action

```java
@Service
@RequiredArgsConstructor  // Lombok generates constructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    // productRepository & productMapper injected automatically by Spring!
}
```

### Auto-Healing Configuration

```java
@Configuration
public class ApplicationConfig {
    @Bean
    public RestTemplate restTemplate() {  // <- Becomes a Spring bean
        return new RestTemplate();
    }
}
```

Later, in any service:

```java
@Service
public class CartService {
    private final RestTemplate restTemplate;
    // Spring automatically injects the bean we defined above!
}
```

---

## Build Process

```bash
mvn clean install
```

Creates:

```
product-service-1.0-SNAPSHOT.jar
├── BOOT-INF/
│   ├── classes/      (compiled code)
│   ├── lib/          (dependencies)
│   └── classpath.idx
├── META-INF/
│   └── MANIFEST.MF
└── org/springframework/boot/loader/  (Spring Boot classloader)
```

### Run JAR

```bash
java -jar product-service-1.0-SNAPSHOT.jar

# Starts embedded Tomcat on port 8081 automatically!
# No separate Tomcat server needed!
```

---

## Property Sources (Precedence)

Spring Boot reads properties from multiple sources (in order):

1. **Command line** (`--server.port=9000`)
2. **Environment variables** (`export SERVER_PORT=9000`)
3. **application.yml** / **application.properties**
4. **application-{profile}.yml** (if profile is active)
5. **Default values** (hardcoded in code)

```bash
# Command line overrides everything
java -jar app.jar --server.port=9000

# Or environment variable
export SERVER_PORT=9000
java -jar app.jar

# Or in application.yml
server:
  port: 9000
```

---

## Profiles (Dev/Test/Prod)

```yaml
# application.yml (always loaded)
spring:
  application:
    name: product-service

---
# application-dev.yml (only in dev)
spring:
  datasource:
    url: jdbc:h2:mem:productdb  # in-memory H2
```

Activate profile:

```bash
java -jar app.jar --spring.profiles.active=dev
```

---

## Real-World Analogy

Spring Boot is like an **all-in-one starter kit**:

- **Without Spring Boot**: Buy engine, wheels, frame, seats, wiring separately → assemble → test → hope it works
- **With Spring Boot**: Pre-assembled starter kit → install dependencies → run!

---

## Common Spring Boot Interview Questions

**Q: What's the difference between Spring and Spring Boot?**
- **Spring**: Framework requiring extensive configuration
- **Spring Boot**: Opinionated, auto-configured Spring for rapid development

**Q: What does @SpringBootApplication do?**
- Combines `@Configuration` + `@ComponentScan` + `@EnableAutoConfiguration`
- Marks main class + enables Spring Boot auto-configuration

**Q: How does Spring Boot startup differ from traditional servlet apps?**
- No need to deploy to Tomcat
- No web.xml configuration
- Embedded server starts automatically

---

## Further Reading

- **Spring Boot Official**: https://spring.io/projects/spring-boot
- **Spring Boot Starters**: https://spring.io/projects/spring-boot#learn
- **Spring Framework Reference**: https://spring.io/projects/spring-framework

---

**Next**: Learn how Spring Cloud extends Spring Boot for distributed systems → [Spring Cloud](./03-spring-cloud.md)

