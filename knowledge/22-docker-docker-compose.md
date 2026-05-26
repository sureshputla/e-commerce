# Docker & Docker Compose

## What is Docker?

**Docker** is a **containerization platform** – it packages your application and all dependencies (Java, libraries, config) into a self-contained **container** that runs identically everywhere.

```
BEFORE Docker (Fragile):
Laptop: "Works on my machine!"
    ├─ Java 17 installed
    ├─ Maven 3.8 installed
    ├─ Kafka running locally
    └─ Environment variables set

Server: "Doesn't work!"
    ├─ Java 11 installed
    ├─ Maven 3.6 installed
    ├─ Kafka version mismatch
    └─ Different environment variables

❌ "Dependency hell"
```

```
WITH Docker (Reliable):
application.jar
└─ INSIDE Docker container:
   ├─ Java 17 (pre-installed in image)
   ├─ Kafka client (included)
   ├─ Configuration (included)
   └─ Everything needed

Laptop: Works ✓
Server: Works ✓
Production: Works ✓

SAME container everywhere!
```

---

## Key Concepts

### Image
**Blueprint** for creating containers. Like a template or class definition.

```dockerfile
# Dockerfile (defines image)
FROM openjdk:17-slim           # Base image
WORKDIR /app                   # Working directory
COPY app.jar app.jar           # Copy JAR
EXPOSE 8080                    # Port
CMD ["java", "-jar", "app.jar"] # Command to run
```

### Container
**Running instance** of an image. Like an object created from a class.

```bash
docker run myapp:1.0
# Creates container from myapp:1.0 image
# Runs the application
```

### Registry
**Repository** of images. Like GitHub for Docker images.

```
Docker Hub: public registry
    ├── openjdk:17-slim      (official Java image)
    ├── postgres:latest      (official PostgreSQL)
    └── nginx:latest         (official nginx)

You can also:
    mvn spring-boot:build-image  (builds image automatically)
    docker push myrepo/myapp:1.0 (push to registry)
```

---

## In Our Project

### Our Dockerfile (if we had one)

For each service:

```dockerfile
FROM openjdk:17-slim
WORKDIR /app
COPY target/order-service-1.0-SNAPSHOT.jar app.jar
EXPOSE 8084
ENV SPRING_PROFILES_ACTIVE=production
CMD ["java", "-jar", "app.jar"]
```

Build & run:

```bash
# Build image
docker build -t order-service:1.0 .

# Run container
docker run -p 8084:8084 order-service:1.0
```

### docker-compose.yml (For Local Development)

```yaml
version: '3.8'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.6.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    depends_on:
      - kafka
    ports:
      - "8989:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092
```

Start everything:

```bash
docker-compose up -d
```

Containers created & running:
- `zookeeper` listening on `localhost:2181`
- `kafka` listening on `localhost:9092`
- `kafka-ui` accessible at `http://localhost:8989`

---

## Docker Workflow

### 1. Create Dockerfile

```dockerfile
FROM openjdk:17-slim

WORKDIR /app

COPY target/product-service-1.0-SNAPSHOT.jar app.jar

EXPOSE 8081

ENV JAVA_OPTS="-Xmx512M"

CMD ["java", $JAVA_OPTS, "-jar", "app.jar"]
```

### 2. Build Image

```bash
docker build -t product-service:1.0 .
```

Produces: `product-service:1.0` image (locally stored)

### 3. Run Container

```bash
docker run \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:h2:mem:productdb \
  product-service:1.0
```

Container running, listening on `localhost:8081`!

### 4. Verify

```bash
curl http://localhost:8081/api/products
# Returns product data!
```

---

## Docker Compose Benefits

**Single file** orchestrates multiple containers:

```bash
docker-compose up      # Start all containers
docker-compose down    # Stop all containers
docker-compose logs    # View logs from all containers
docker-compose ps      # Show running containers
```

Without Compose, you'd run each container separately:

```bash
docker run -d zookeeper...
docker run -d kafka...
docker run -d kafka-ui...
# Complex! Hard to manage!
```

---

## Container Networking

Docker Compose creates internal network:

```yaml
# Services can communication by name!
services:
  kafka:
    ...
  
  payment-service:
    environment:
      KAFKA_SERVER: kafka:29092  # Using service name!
```

`payment-service` refers to `kafka` by service name, Docker resolves to IP.

---

## Advantages

✅ **Consistency** – Same container everywhere  
✅ **Isolation** – Container doesn't affect host  
✅ **Scalability** – Easily run multiple instances  
✅ **Deployment** – Just `docker run` → instant setup  
✅ **CI/CD** – Automated builds & deployments  

---

## Disadvantages

❌ **Learning curve** – Docker commands, registry, networking  
❌ **Storage** – Containers don't persist data (use volumes)  
❌ **Networking** – More complex multi-container networking  
❌ **Debugging** – Harder to debug inside containers  

---

## Real-World Analogy

Docker is like **shipping containers**:

- **Before Docker**: Cargo on ship (app on laptop)
  - Move to new ship (laptop to server)
  - Cargo moved badly, damaged
  - Different cargo handling procedures
  
- **With Docker**: Sealed shipping containers
  - Same container on any ship
  - Cargo protected, unchanged
  - Standardized loading procedures
  - Same result everywhere!

---

## Common Docker Commands

```bash
# Images
docker images                    # List images
docker build -t name:version .  # Build image
docker push name:version        # Push to registry

# Containers
docker ps                        # List running containers
docker run image:version        # Run container
docker logs container_id        # View logs
docker stop container_id        # Stop container
docker rm container_id          # Delete container

# Compose
docker-compose up -d            # Start services (background)
docker-compose down             # Stop services
docker-compose logs -f          # Follow logs
docker-compose restart          # Restart services
```

---

## Further Reading

- **Docker Official**: https://docs.docker.com/
- **Docker Compose**: https://docs.docker.com/compose/
- **Docker Hub**: https://hub.docker.com/

---

**Next**: Learn about build tools → [Apache Maven](./21-apache-maven.md)

