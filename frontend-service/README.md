# Frontend Service

Serves the Thymeleaf-based store UI. All API calls from the browser go directly to the **API Gateway** (port 8080), which routes them to the appropriate backend service.

## Port
`8090`

## Technologies
- Spring Boot 3.3.5
- Thymeleaf (server-side template)
- Vanilla JavaScript (no external JS framework)
- Spring Cloud Eureka Client

## How it works

```
Browser → http://localhost:8090/  → HomeController → index.html
Browser JS → http://localhost:8080/api/...  → API Gateway → backend services
```

The `HomeController` injects `http://localhost:8080` as `apiGatewayUrl` into the template. JavaScript picks it up from `window.API_GATEWAY` and prefixes all fetch calls with it.

This design means:
- The frontend service is purely a static server – **no API calls go through it**.
- CORS is handled by the API Gateway (`allowedOrigins: "*"` in its config).

## Pages

| Path | Description |
|------|-------------|
| `/` | Full store: filter products, add to cart, manage wishlist, checkout |

## Frontend Features
- Product listing with live filter + search
- Add/remove wishlist items
- Add/remove cart items with running total
- Checkout (card / UPI / COD) – triggers the backend Saga
- Async payment status message

## Run
```bash
# All backend services + Kafka should be running
cd frontend-service
mvn spring-boot:run
```

Then open **http://localhost:8090** in your browser.

## Customising the API Gateway URL
If you run the gateway on a different host or port, update `application.yml`:
```yaml
# No config needed in frontend-service – the URL is hardcoded in HomeController.
# Edit HomeController.java:
model.addAttribute("apiGatewayUrl", "http://my-gateway-host:8080");
```

