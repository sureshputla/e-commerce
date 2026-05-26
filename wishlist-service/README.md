# Wishlist Service

Lets users save products for later. Calls product-service to fetch product details and denormalizes them for resilience.

## Port
`8083`

## Technologies
- Spring Boot 3.3.5 (Web + JPA)
- H2 In-Memory Database
- Lombok
- Spring Cloud OpenFeign + LoadBalancer
- Resilience4j (Feign fallback)
- Spring Cloud Eureka Client
- Springdoc OpenAPI

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET    | `/api/wishlist` | Get all wishlist items |
| POST   | `/api/wishlist/{productId}` | Add product to wishlist (idempotent) |
| DELETE | `/api/wishlist/{productId}` | Remove product from wishlist |

Response shape (list of product-like objects, backward-compatible):
```json
[
  { "id": 3, "name": "Stacking Ring Toy", "category": "Toys", "brand": "PlayBud", "price": 499 }
]
```

## Inter-Service Calls (Feign)
| Service | Endpoint | Purpose |
|---------|----------|---------|
| product-service | `GET /api/products/{id}` | Fetch product details on add |

## Design Notes
- Wishlist items **denormalize** product data (name, brand, category, price) at add time.
- If product-service is down, the fallback returns a placeholder so the add is rejected gracefully.
- Add operation is **idempotent** – adding the same product twice is a no-op.

## Run
```bash
# Prerequisites: service-registry, product-service
cd wishlist-service
mvn spring-boot:run
```

## Useful URLs
| URL | Description |
|-----|-------------|
| http://localhost:8083/swagger-ui.html | Swagger UI |
| http://localhost:8083/h2-console | H2 Console (`jdbc:h2:mem:wishlistdb`) |

