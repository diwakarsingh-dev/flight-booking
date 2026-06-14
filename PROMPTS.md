Prompt 1:
Create a Spring Boot 3.5.15 project with Java 17 using Maven. Package name: com.flight.booking.

Add these dependencies to pom.xml:
- spring-boot-starter-web
- spring-boot-starter-validation
- lombok
- spring-boot-starter-actuator (for health/metrics/observability endpoints)
- micrometer-registry-prometheus (for future Prometheus/Grafana integration)
- spring-boot-starter-test (test scope)

Create the following:

1. Domain models (use Lombok annotations — @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor):
   - Flight: flightNumber (String, unique), origin (String), destination (String), departureTime (LocalDateTime), totalSeats (int), availableSeats (int)
   - Booking: bookingId (UUID, auto-generated), flightNumber (String), passengerName (String), passengerEmail (String), numberOfSeats (int), bookingTime (LocalDateTime)

2. In-memory repositories using ConcurrentHashMap:
   - FlightRepository: stores flights keyed by flightNumber. Pre-load 5 sample flights on startup via @PostConstruct.
   - BookingRepository: stores bookings keyed by bookingId.
   - Use @Slf4j (Lombok) for logging in both repositories. Log at DEBUG level for all CRUD operations.

3. DTOs (use Java records for immutability — do NOT use Lombok on records):
   - BookingRequest: flightNumber, passengerName, passengerEmail, numberOfSeats (all validated with Jakarta Bean Validation — @NotBlank, @Email, @Min(1))
   - BookingResponse: bookingId, flightNumber, passengerName, numberOfSeats, bookingTime, status message

4. application.yml configuration:
   - Enable actuator endpoints: health, info, metrics, prometheus
   - Set logging level for com.flight.booking to DEBUG
   - Set server port to 8080

Proper separation into packages: model, repository, dto, config.

Create a BookingService in com.flight.booking.service that:

1. Uses @Slf4j and @RequiredArgsConstructor (Lombok).
2. Has a bookFlight(BookingRequest) method that:
   - Logs the incoming request at INFO level with flightNumber and passengerName.
   - Looks up the flight by flightNumber; throws FlightNotFoundException (404) if not found.
   - Uses ConcurrentHashMap.compute() on the flight store for atomic, lock-free seat reservation — do NOT use synchronized blocks.
   - If requested seats > available seats, throws InsufficientSeatsException (409 Conflict) with a message showing requested vs available seats.
   - Creates a Booking record, stores it, and returns BookingResponse.
   - Logs successful booking at INFO level with bookingId and seats remaining.
   - Logs failures at WARN level with reason.

Also create:
- Custom exceptions in com.flight.booking.exception package:
  - FlightNotFoundException extends RuntimeException
  - InsufficientSeatsException extends RuntimeException
- A GlobalExceptionHandler (@RestControllerAdvice) with @Slf4j that:
  - Handles FlightNotFoundException → 404
  - Handles InsufficientSeatsException → 409
  - Handles MethodArgumentNotValidException → 400 (extract field-level errors into a map)
  - Handles generic Exception → 500
  - Returns consistent JSON: { "error": string, "message": string, "timestamp": ISO-8601 }
  - Logs all exceptions: WARN for 4xx, ERROR for 5xx (include stack trace for 5xx only)
  - Uses @RequiredArgsConstructor (Lombok) if any dependencies are injected

Prompt 2:
Create a FlightBookingController in com.flight.booking.controller:

Use @Slf4j (Lombok) for logging and @RequiredArgsConstructor (Lombok) for constructor injection — do NOT write explicit constructors or use @Autowired.

Single endpoint:
- POST /api/bookings — accepts @Valid @RequestBody BookingRequest, returns 201 Created with BookingResponse and a Location header (URI: /api/bookings/{bookingId}).

Log request entry/exit at INFO level (e.g., "Received booking request for flight {}" and "Booking completed: {}").
Use ResponseEntity with proper HTTP status codes.

Prompt 3:
Add the following tests:

1. BookingServiceTest (unit test with JUnit 5 + Mockito):
   - Test successful booking decrements seats
   - Test booking with insufficient seats throws InsufficientSeatsException
   - Test booking for non-existent flight throws FlightNotFoundException
   - Test concurrent bookings don't oversell (use ExecutorService with 10 threads each booking 1 seat on a flight with 5 seats — assert exactly 5 succeed and 5 fail)

2. FlightBookingControllerIntegrationTest (@SpringBootTest + MockMvc):
   - Test POST /api/bookings returns 201 for valid request
   - Test POST /api/bookings returns 400 for missing required fields
   - Test POST /api/bookings returns 404 for unknown flight
   - Test POST /api/bookings returns 409 when seats exhausted

Use AssertJ for assertions.

Prompt 4:
Create a comprehensive README.md with:
1. Project title and one-line description (Flight Ticket Booking API)
2. Tech stack: Java 17, Spring Boot 3.5.15, Maven, Lombok, Micrometer/Actuator
3. How to build and run: mvn clean install && mvn spring-boot:run
4. API documentation with curl example for POST /api/bookings with sample JSON body
5. Sample success response (201) and error responses (400, 404, 409)
6. Observability section:
   - Health check: GET /actuator/health
   - Metrics: GET /actuator/metrics
   - Prometheus endpoint: GET /actuator/prometheus
   - Explain how to connect to Grafana for dashboards
7. Design decisions section: thread-safety (ConcurrentHashMap.compute), Lombok for reduced boilerplate, records for DTOs, Jakarta Bean Validation, structured logging for observability
8. "What I would improve with more and sufficient time":
   - Persistent storage (JPA/H2)
   - Distributed tracing with OpenTelemetry + Jaeger
   - Custom Micrometer metrics (booking counter, seat utilization gauge)
   - Correlation IDs via MDC for request tracing
   - Swagger/OpenAPI documentation
   - Cancellation/refund API
   - Request idempotency keys
   - Docker + docker-compose with Prometheus + Grafana
   - Circuit breaker patterns for external integrations

Prompt 5:
Update Request Mapping URL"/flight" on class level and "/booking" at method level
Under BookingRequest class, if we are accepting more then 1 seat then we need to take all passengers details, so it should be List of passenger name, update sample curl and respective usages

Prompt 6:
Tested and validated all basic pass and failed scenarios from postman

Prompt 7:
Added MDC correlation ID filter (UUID per request) for structured log tracing

Prompt 8:
Verified ConcurrentHashMap.compute() atomicity — no race conditions in seat decrement
Ensured Lombok @builder usage is consistent — added @builder on Booking for clean construction
Tightened validation: @min(1) on numberOfSeats, @Email on passengerEmail, @SiZe(max=100) on names
