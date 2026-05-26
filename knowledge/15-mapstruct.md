# MapStruct – Object Mapping

## The Problem

Converting between **Entity** ↔ **DTO** (Data Transfer Object) is tedious.

```java
// Entity (database)
@Entity
public class Product {
    private int id;
    private String name;
    private String category;
    // 20 more fields...
}

// DTO (API response)
public class ProductDto {
    private int id;
    private String name;
    private String category;
    // 20 same fields
}

// Manual Mapping (Error-prone! Repetitive!)
public ProductDto toDto(Product product) {
    ProductDto dto = new ProductDto();
    dto.setId(product.getId());
    dto.setName(product.getName());
    dto.setCategory(product.getCategory());
    // ... 20 more manual assignments!
    
    // If you add a field to Product, forget to add here:
    // ❌ Silent bug! DTO missing field!
    
    return dto;
}
```

**With MapStruct**:

```java
@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDto toDto(Product product);
    Product toEntity(ProductDto dto);
}

// MapStruct generates the mapping code!
// Add new field? Compiler error → catches immediately!
```

---

## What is MapStruct?

**MapStruct** is an **annotation processor** that generates **type-safe, high-performance object mappers** at compile time.

### Key Idea

```
Entity fields: id, name, category, price
    ↓
MapStruct reads annotations
    ↓
Generates mapper code:
    dto.setId(entity.getId());
    dto.setName(entity.getName());
    ... (blazingly fast – direct assignment!)
    ↓
Compiled bytecode (.class)
    ↓
Zero reflection overhead vs Jackson!
```

---

## How It Works

### Step 1: Define Mapper Interface

```java
@Mapper(componentModel = "spring")  // Use Spring as component model
public interface ProductMapper {
    
    // Simple mapping
    ProductDto toDto(Product product);
    
    // Reverse mapping
    Product toEntity(ProductDto dto);
    
    // Mapping collections
    List<ProductDto> toDtoList(List<Product> products);
}
```

### Step 2: MapStruct Generates Implementation

At compile time, MapStruct generates:

```java
// Generated code (you don't write this!)
@Component
public class ProductMapperImpl implements ProductMapper {
    
    @Override
    public ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setCategory(product.getCategory());
        productDto.setBrand(product.getBrand());
        productDto.setPrice(product.getPrice());
        
        return productDto;
    }
    
    @Override
    public Product toEntity(ProductDto dto) {
        if (dto == null) {
            return null;
        }
        
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setBrand(dto.getBrand());
        product.setPrice(dto.getPrice());
        
        return product;
    }
    
    @Override
    public List<ProductDto> toDtoList(List<Product> products) {
        if (products == null) {
            return null;
        }
        
        List<ProductDto> list = new ArrayList<ProductDto>(products.size());
        for (Product product : products) {
            list.add(toDto(product));
        }
        
        return list;
    }
}
```

### Step 3: Use in Services

```java
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;  // Injected by Spring!
    
    public List<ProductDto> getProducts() {
        List<Product> entities = productRepository.findAll();
        return productMapper.toDtoList(entities);  // MapStruct mapper!
    }
}
```

---

## In Our Project

### ProductMapper

```java
/**
 * MapStruct mapper – zero-boilerplate entity ↔ DTO conversion.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDto toDto(Product product);

    List<ProductDto> toDtoList(List<Product> products);

    Product toEntity(ProductDto dto);
}
```

### Service Using Mapper

```java
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    
    public List<ProductDto> getProducts(String category, String brand, Integer maxPrice, String search) {
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        
        return productRepository.findAll().stream()
                .filter(p -> isAll(category) || p.getCategory().equals(category))
                .filter(p -> isAll(brand) || p.getBrand().equals(brand))
                .filter(p -> maxPrice == null || maxPrice < 0 || p.getPrice() <= maxPrice)
                .filter(p -> normalizedSearch.isBlank()
                        || p.getName().toLowerCase(Locale.ROOT).contains(normalizedSearch))
                .map(productMapper::toDto)  // ← MapStruct mapper!
                .toList();
    }
}
```

---

## Advanced Features

### Custom Field Mapping

```java
@Mapper(componentModel = "spring")
public interface OrderMapper {
    
    // Default: name → name
    // Custom: payment.method → paymentMethod
    @Mapping(source = "payment.method", target = "paymentMethod")
    OrderDto toDto(Order order);
}
```

### Date Formatting

```java
@Mapper(componentModel = "spring")
public interface EventMapper {
    
    @Mapping(source = "createdAt", target = "createdAtFormatted",
             dateFormat = "dd-MM-yyyy HH:mm:ss")
    EventDto toDto(Event event);
}
```

### Conditional Mapping

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "email", ignore = true)  // Don't map email
    UserDto toDto(User user);
    
    @Mapping(source = "role", target = "roleName", 
             qualifiedByName = "roleToRoleName")
    UserDto toDtoWithRole(User user);
    
    @Named("roleToRoleName")
    default String roleToRoleName(Role role) {
        return role == null ? null : role.getDisplayName();
    }
}
```

---

## MapStruct vs Other Mapping Tools

| Tool | Approach | Performance | Flexibility |
|------|----------|-------------|------------|
| **MapStruct** | Compile-time code generation | ✓✓✓ Fastest (direct assignment) | ✓✓ Good (annotation-driven) |
| **Jackson** | Reflection-based | ✓ Good | ✓✓✓ Excellent (flexible) |
| **ModelMapper** | Reflection-based | ✓ OK | ✓✓ Good |
| **Selma** | Compile-time (alternative) | ✓✓✓ Fast | ✓✓ OK |
| **Manual** | Hand-coded | ✓✓✓ Fastest (optimized) | ✓✓✓ Perfect control |

**Our choice**: MapStruct – fast, safe, minimal boilerplate.

---

## Benefits

✅ **Compile-time safety** – Add field to Entity → compiler error if DTO not updated  
✅ **Zero reflection** – Direct assignment, very fast  
✅ **IDE support** – Click "implement" → MapStruct generates code  
✅ **Easy debugging** – Generated source visible, can debug step-through  
✅ **Spring integration** – MapStruct bean automatically registered  

---

## Setup

### pom.xml

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>1.5.5.Final</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### IDE

BuildPath configuration usually auto-detected. If not:
1. Project → Service Factories
2. Enable annotation processing
3. Rebuild project

---

## Real-World Analogy

MapStruct is like a **template printer**:

- **Without MapStruct**: Manually hand-write each copy from template
- **With MapStruct**: Feed template once, printer auto-generates copies
- **Safe**: If template changes, printer auto-updates all copies (compiler error if any issue)
- **Fast**: Printer runs at build-time, copies are ready instantly

---

## Further Reading

- **MapStruct Official**: https://mapstruct.org/
- **MapStruct Documentation**: https://mapstruct.org/documentation/stable/reference/html/

---

**Next**: Learn about containerization → [Docker & Docker Compose](./22-docker-docker-compose.md)

