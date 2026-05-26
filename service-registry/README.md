# Service Registry

Eureka Service Registry – central discovery server for all microservices.

## Port
`8761`

## Technologies
- Spring Boot 3.3.5
- Spring Cloud Netflix Eureka Server

## What it does
All microservices register themselves with Eureka on startup. The API Gateway and Feign clients use Eureka to discover service instances and apply client-side load balancing.

## Run
```bash
cd service-registry
mvn spring-boot:run
```

## Useful URLs
| URL | Description |
|-----|-------------|
| http://localhost:8761 | Eureka Dashboard |
| http://localhost:8761/actuator/health | Health check |

## Notes
- Must be started **before** any other service.
- Self-registration is disabled (`register-with-eureka: false`) since this IS the registry.
- Self-preservation is disabled for development ease.

