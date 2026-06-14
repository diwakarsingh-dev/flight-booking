# Flight Ticket Booking API

A RESTful flight booking service with thread-safe seat reservation, built with Spring Boot and production-ready observability.

---

## Tech Stack

- **Java 17**
- **Spring Boot 3.5.15**
- **Maven** (build & dependency management)
- **Lombok** (boilerplate reduction)
- **Micrometer + Prometheus** (metrics & monitoring)
- **Spring Boot Actuator** (health, info, metrics endpoints)
- **Jakarta Bean Validation** (request validation)

---

## Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

Or direct run "src/main/java/com/flight/booking/FlightBookingApplication.java" under any IDE

The application starts on `http://localhost:8080`.

---

## API Documentation

### POST /flight/booking

Book seats on a flight. The number of seats is determined by the number of passenger names provided.

**Request:**

```bash
curl -X POST http://localhost:8080/flight/booking \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "FL001",
    "passengerNames": ["Diwakar Singh", "Deepak Singh"],
    "passengerEmail": "john.doe@example.com"
  }'
```

### Success Response (201 Created)

```json
{
  "bookingId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "flightNumber": "FL001",
  "passengerNames": ["Diwakar Singh", "Deepak Singh"],
  "numberOfSeats": 2,
  "bookingTime": "2026-06-15T10:30:00",
  "status": "Booking confirmed"
}
```

**Headers:**
```
Location: /api/bookings/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

### Error Responses

**400 Bad Request** (validation failure):

```json
{
  "error": "Validation Failed",
  "message": "{passengerNames=At least one passenger name is required, flightNumber=Flight number is required}",
  "timestamp": "2026-06-15T10:30:00"
}
```

**404 Not Found** (unknown flight):

```json
{
  "error": "Flight Not Found",
  "message": "Flight not found: FL999",
  "timestamp": "2026-06-15T10:30:00"
}
```

**409 Conflict** (insufficient seats):

```json
{
  "error": "Insufficient Seats",
  "message": "Requested 10 seats but only 3 available on flight FL001",
  "timestamp": "2026-06-15T10:30:00"
}
```

---

## Observability

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Metrics

```bash
curl http://localhost:8080/actuator/metrics
```

### Prometheus Endpoint

```bash
curl http://localhost:8080/actuator/prometheus
```

### Connecting to Grafana

1. Run Prometheus and configure it to scrape `http://host.docker.internal:8080/actuator/prometheus`.
2. Add Prometheus as a data source in Grafana (`http://prometheus:9090`).
3. Import a Spring Boot dashboard (e.g., Grafana Dashboard ID **19004**) or create custom panels for JVM metrics, HTTP request rates, and response times.
4. Use PromQL queries like `http_server_requests_seconds_count{uri="/flight/booking"}` to monitor booking traffic.

---

## Design Decisions

| Decision | Rationale |
|----------|-----------|
| **ConcurrentHashMap.compute()** | Atomic, lock-free seat reservation without synchronized blocks — prevents overselling under concurrent load |
| **Lombok** | Eliminates boilerplate (getters, setters, constructors, loggers) while keeping models concise |
| **Java Records for DTOs** | Immutable by design, zero boilerplate, ideal for request/response objects |
| **Jakarta Bean Validation** | Declarative input validation with automatic error propagation via @Valid |
| **Structured logging (@Slf4j)** | Consistent log format for observability; DEBUG for repository ops, INFO for business events, WARN for failures |
| **GlobalExceptionHandler** | Consistent JSON error responses across all failure modes with appropriate HTTP status codes |

---

## What I Would Improve With More Time

- **Persistent storage** — Replace in-memory maps with JPA + H2/PostgreSQL for durability
- **Distributed tracing** — OpenTelemetry + Jaeger for end-to-end request tracing across services
- **Custom Micrometer metrics** — Booking counter, seat utilization gauge, reservation latency histogram
- **Correlation IDs via MDC** — Inject unique request IDs into every log line for request tracing
- **Swagger/OpenAPI documentation** — Auto-generated API docs with springdoc-openapi
- **Cancellation/refund API** — DELETE /api/bookings/{id} with seat restoration
- **Request idempotency keys** — Prevent duplicate bookings on network retries
- **Docker + docker-compose** — Containerized app with Prometheus + Grafana stack out of the box
- **Circuit breaker patterns** — Resilience4j for external service integrations (payment, notifications)
