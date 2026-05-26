# Product Service

Manages the product catalog – listing, searching, and filtering products.

## Port
`8081`

## Technologies
- Spring Boot 3.3.5 (Web + JPA)
- H2 In-Memory Database
- Lombok (boilerplate elimination)
- MapStruct (entity ↔ DTO mapping)
- Springdoc OpenAPI
- Spring Cloud Eureka Client

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/filters` | Get all categories and brands |
| GET | `/api/products` | List products (supports `?category=`, `?brand=`, `?maxPrice=`, `?search=`) |
| GET | `/api/products/{id}` | Get a single product by ID |

## Data
Products are pre-loaded via `data.sql` on startup (8 baby products):
- Soft Dry Diapers, Anti-Colic Bottle, Stacking Ring Toy, Battery Jeep, Rocking Cradle, Cotton Cloth Set, Training Pants, Sipper Bottle

## Run
```bash
# service-registry must be running
cd product-service
mvn spring-boot:run
```

## Useful URLs
| URL | Description |
|-----|-------------|
| http://localhost:8081/swagger-ui.html | Swagger UI |
| http://localhost:8081/h2-console | H2 Console (JDBC URL: `jdbc:h2:mem:productdb`) |
| http://localhost:8081/actuator/health | Health |

## Design Notes
- **MapStruct** generates `ProductMapper` at compile time – no reflection overhead.
- **Lombok** `@Data @Builder @Slf4j` eliminates getters/setters/constructors boilerplate.
- Service logic mirrors the original monolith's filtering but operates on JPA entities.

