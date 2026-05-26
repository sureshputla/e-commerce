# Lombok – Boilerplate Elimination

## The Problem

Java requires lots of **boilerplate code** – repetitive code that doesn't add business value.

```java
public class Product {
    private int id;
    private String name;
    private String category;
    private String brand;
    private int price;
    
    // ======================== BOILERPLATE START ========================
    
    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getBrand() { return brand; }
    public int getPrice() { return price; }
    
    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setPrice(int price) { this.price = price; }
    
    // Constructors
    public Product() {}
    public Product(int id, String name, String category, String brand, int price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.price = price;
    }
    
    // toString, equals, hashCode
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", brand='" + brand + '\'' +
                ", price=" + price +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id &&
                price == product.price &&
                Objects.equals(name, product.name) &&
                Objects.equals(category, product.category) &&
                Objects.equals(brand, product.brand);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name, category, brand, price);
    }
    
    // ======================== BOILERPLATE END ========================
    
    // Maybe 100+ lines just for infrastructure!
}
```

**With Lombok**, same class becomes:

```java
@Data
@Builder
public class Product {
    private int id;
    private String name;
    private String category;
    private String brand;
    private int price;
    // 10 lines instead of 100+ ✨
}
```

---

## What is Lombok?

**Lombok** is an annotation processor that **generates boilerplate code at compile time**. You write annotations, Lombok generates the code.

### Common Annotations

| Annotation | Generates |
|-----------|-----------|
| `@Data` | Getters, setters, toString, equals, hashCode |
| `@Builder` | Builder pattern (fluent API for object creation) |
| `@NoArgsConstructor` | No-argument constructor |
| `@AllArgsConstructor` | Constructor with all fields |
| `@RequiredArgsConstructor` | Constructor for final fields |
| `@Getter` | Just getters |
| `@Setter` | Just setters |
| `@Slf4j` | Static logger field |
| `@Value` | Immutable version of @Data |
| `@EqualsAndHashCode` | Just equals/hashCode |
| `@ToString` | Just toString |

---

## How It Works

### Compilation Process

```
Source Code (with Lombok annotations)
    │
    ├─ Javac compiler
    │
    ├─ Lombok annotation processor (plugin)
    │   └─ Generates getters, setters, etc.
    │
    ▼
Compiled bytecode (with generated methods)
    │
    ▼
.class files (seemingly have all methods but you didn't write them!)
```

**No runtime overhead!** Generated at <compile time>, just like you wrote it.

---

## In Our Project

### Product Entity

```java
@Entity
@Table(name = "products")
@Data              // ← Generates: getters, setters, toString, equals, hashCode
@Builder           // ← Generates: builder pattern
@NoArgsConstructor // ← Generates: empty constructor for JPA
@AllArgsConstructor
public class Product {
    @Id
    private Integer id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private String brand;
    
    @Column(nullable = false)
    private Integer price;
    
    @Builder.Default
    private Integer stockQuantity = 0;
}
```

### Service with Logging

```java
@Service
@Slf4j  // ← Generates: private static final Logger log = LoggerFactory.getLogger(...);
@RequiredArgsConstructor  // ← Generates: constructor with final fields
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    public List<ProductDto> getProducts(String category) {
        log.debug("Fetching products for category={}", category);  // ← log available!
        return productRepository.findByCategory(category);
    }
}
```

### Builder Pattern Usage

```java
// Without Lombok (verbose):
Product product = new Product();
product.setId(1);
product.setName("Diaper");
product.setCategory("Diapers");
product.setBrand("TinyCare");
product.setPrice(799);

// With Lombok @Builder (fluent, readable):
Product product = Product.builder()
    .id(1)
    .name("Diaper")
    .category("Diapers")
    .brand("TinyCare")
    .price(799)
    .build();

// Even better with defaults:
Product product = Product.builder()
    .id(1)
    .name("Diaper")
    .price(799)
    .build();
    // category, brand default to null
    // stockQuantity defaults to 0 (@Builder.Default)
```

---

## Common Annotations Explained

### @Data
```java
@Data
public class User {
    private String name;
    private int age;
}

// Generates:
// - getters: getName(), getAge()
// - setters: setName(), setAge()
// - toString()
// - equals() & hashCode()
// - constructor: NO! Use @NoArgsConstructor or @AllArgsConstructor too
```

### @Builder
```java
@Builder
public class Order {
    private String orderId;
    private List<Item> items;
    private int total;
}

// Usage:
Order order = Order.builder()
    .orderId("123")
    .items(List.of(...))
    .total(999)
    .build();

// Builds intermediate OrderBuilder before creating Order
// Very readable!
```

### @RequiredArgsConstructor (Best for Dependency Injection)
```java
@Service
@RequiredArgsConstructor  // Constructor for all final fields
public class OrderService {
    private final OrderRepository orderRepository;      // final → required in constructor
    private final PaymentService paymentService;        // final → required
    private String config = "default";                  // not final → not in constructor
}

// Lombok generates:
// public OrderService(OrderRepository orderRepository, PaymentService paymentService) {
//     this.orderRepository = orderRepository;
//     this.paymentService = paymentService;
// }

// Spring injects via this constructor! ✓
```

### @Slf4j
```java
@Slf4j
public class OrderService {
    public void processOrder(Order order) {
        log.debug("Processing order: {}", order.getId());
        log.info("Order processed successfully");
        log.error("Order failed", exception);
    }
}

// Lombok generates:
// private static final Logger log = LoggerFactory.getLogger(OrderService.class);
```

### @Value (Immutable)
```java
@Value  // Like @Data but immutable
public class PaymentResult {
    String paymentId;
    boolean success;
    String message;
}

// Generated code:
// - getters only (no setters)
// - All fields private final
// - Constructor with all fields
// - equals, hashCode, toString
// - Can't be modified after creation!
```

---

## Advantages

✅ **Less boilerplate** – focus on business logic  
✅ **Less bugs** – Lombok generates correct code  
✅ **Easier refactoring** – add field → Lombok updates getters/setters automatically  
✅ **Consistent** – all generated code follows same pattern  
✅ **No runtime cost** – pure compile-time generation  

---

## Disadvantages

❌ **IDE integration** – Not all IDEs understand Lombok initially  
❌ **Magic** – Generated code is invisible (though most IDEs show it now)  
❌ **Debugging** – Breakpoints in generated code can be confusing  
❌ **Learning curve** – Need to know which annotations exist  

---

## Real-World Analogy

Lombok is**IDE macros on steroids**:

- **Without Lombok**: Manually write 100 lines of getters/setters
- **With Lombok**: Write one annotation, Lombok expands it to 100 lines
- Like a **code template** that auto-expands!

The computer does repetitive work, you focus on business logic!

---

## Configuration

### pom.xml Setup

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.32</version>
    <scope>provided</scope>  <!-- Only needed at compile-time -->
</dependency>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.32</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### IDE Setup

**IntelliJ**:
1. File → Settings → Plugins
2. Search "Lombok"
3. Install and restart

**Eclipse**:
1. Download Lombok JAR
2. Run `java -jar lombok.jar`
3. Point to Eclipse installation
4. Restart

---

## Further Reading

- **Lombok Official**: https://projectlombok.org/
- **All Annotations**: https://projectlombok.org/features/all

---

**Next**: Learn about object mapping → [MapStruct](./15-mapstruct.md)

