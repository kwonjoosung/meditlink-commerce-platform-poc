<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-03-06 | Updated: 2026-03-06 -->

# meditlink-commerce-client Module

## Purpose

Integration layer (BFF) that exposes REST APIs to external clients while orchestrating gRPC calls to the core commerce service. Handles request/response mapping, HTTP adapters, admin endpoints, webhooks, and request logging with Liquibase-managed PostgreSQL schema.

**Key Responsibilities:**
- REST BFF API surface for product/price/coupon operations
- gRPC client orchestration and outbound gateway
- Request logging and audit trail persistence
- Admin and webhook endpoints
- Spring Boot 4.0 dependency injection and transactional management

---

## Key Files

| File | Purpose | Key Concepts |
|------|---------|--------------|
| `IntegrationLayerApplication.java` | Spring Boot application entry point | Spring Boot autoconfiguration, component scanning |
| `config/CoreGrpcClientConfig.java` | gRPC client bean factory | ManagedChannel lifecycle, BlockingStub beans, reusable channel pattern |
| `gateway/grpc/CoreProductGrpcGateway.java` | Outbound adapter to core service gRPC | Request/response mapping, protocol buffer serialization, gateway pattern |
| `orchestration/ProductOrchestrationService.java` | Business orchestration layer | Flow composition, state logging integration, service delegation |
| `web/ProductBffController.java` | REST API surface | HTTP status codes, request validation, client-friendly DTOs |
| `admin/web/AdminProductController.java` | Admin endpoints | Separate routing, read-only product queries |
| `webhook/web/ProductWebhookController.java` | Async webhook receiver | HTTP 202 Accepted, event payload capture |
| `state/RequestLogService.java` | Audit logging service | Transactional logging, repository pattern |
| `state/IntegrationRequestLogJpaEntity.java` | JPA entity for request logs | Jakarta persistence, audit trail schema |
| `state/IntegrationRequestLogRepository.java` | Spring Data repository | Auto-generated CRUD, repository pattern |

---

## Subdirectories

| Directory | Purpose | Files |
|-----------|---------|-------|
| `admin/web` | Admin-specific REST endpoints | `AdminProductController.java` |
| `config` | Application configuration beans | `CoreGrpcClientConfig.java` |
| `gateway/grpc` | gRPC outbound adapters | `CoreProductGrpcGateway.java` |
| `orchestration` | Business flow composition | `ProductOrchestrationService.java`, `dto/` (7 HTTP DTOs) |
| `orchestration/dto` | HTTP request/response models | `CreateProductHttpRequest`, `IssueCouponHttpRequest/Response`, `PriceQuoteHttpResponse`, `ProductGroupHttpResponse`, `ProductHttpResponse`, `ProductPlanHttpResponse` |
| `state` | Request logging & persistence | `RequestLogService.java`, `IntegrationRequestLogJpaEntity.java`, `IntegrationRequestLogRepository.java` |
| `web` | Client-facing REST API | `ProductBffController.java` |
| `webhook/web` | Webhook event receivers | `ProductWebhookController.java` |

---

## For AI Agents

### Understanding the Flow

1. **Inbound Entry Points:**
   - `ProductBffController` (`/api/bff/*`): Public REST API
   - `AdminProductController` (`/api/admin/products/*`): Admin REST API
   - `ProductWebhookController` (`/api/webhooks/products/*`): Event ingestion

2. **Orchestration Layer:**
   - `ProductOrchestrationService` receives HTTP requests, calls `CoreProductGrpcGateway`, logs via `RequestLogService`
   - Maps HTTP DTOs to gRPC requests and back

3. **gRPC Gateway:**
   - `CoreProductGrpcGateway` translates orchestration calls into protocol buffer messages
   - Uses injected BlockingStubs (`ProductServiceGrpc.ProductServiceBlockingStub`, etc.)
   - Handles type conversions (e.g., `Instant.toEpochMilli()` for timestamps)

4. **Configuration & Lifecycle:**
   - `CoreGrpcClientConfig` creates a single `ManagedChannel` (bean lifecycle)
   - Three service stubs share the same channel for efficiency
   - Channel destruction on shutdown via `destroyMethod="shutdownNow"`

5. **Persistence Layer:**
   - `RequestLogService` inserts audit records transactionally
   - `IntegrationRequestLogJpaEntity` maps to `integration_request_logs` table
   - Tracks: requestType, referenceId, status, payloadJson, createdAt

### Modification Patterns

**Adding a new BFF endpoint:**
1. Add REST method to `ProductBffController` (e.g., `@PostMapping("/new-operation")`)
2. Add orchestration method to `ProductOrchestrationService` (call gateway + log)
3. Add gateway method to `CoreProductGrpcGateway` (gRPC stub call + mapping)
4. Add gRPC service definition to `../meditlink-commerce-common-proto` proto files

**Adding request logging insight:**
- Extend `IntegrationRequestLogJpaEntity` fields
- Update `integration_request_logs` Liquibase migration
- Add parameters to `RequestLogService.log()`

**Changing gRPC target service:**
- Modify host/port in `application.properties` (or `application-local.yml`)
- `CoreGrpcClientConfig` uses `@Value("${core.grpc.host:localhost}")` and `@Value("${core.grpc.port:9090}")`

### Testing Concerns

- Unit test `ProductBffController` with mock `ProductOrchestrationService`
- Unit test `ProductOrchestrationService` with mock `CoreProductGrpcGateway` and `RequestLogService`
- Integration test `CoreProductGrpcGateway` against containerized gRPC service (testcontainers)
- Test transactional logging with `@DataJpaTest` or `@SpringBootTest` + in-memory DB

### Common Pitfalls

- **gRPC channel not closed:** Rely on Spring's `destroyMethod` or manually call `channel.shutdownNow()`
- **Timestamp conversion bugs:** Ensure `Instant` ↔ epoch millis conversions are bidirectional
- **Missing request logging:** Every orchestration method should call `requestLogService.log()` with appropriate status
- **Protocol buffer wire format issues:** Test proto changes against actual core service early

### Documentation Anchors

- Orchestration layer pattern: `ProductOrchestrationService` (coordinates gateway + logging)
- gRPC adapter pattern: `CoreProductGrpcGateway` (protocol buffer serialization)
- REST adapter pattern: `ProductBffController` (HTTP status codes, 201 Created for POST)
- Configuration pattern: `CoreGrpcClientConfig` (shared ManagedChannel, stub injection)

---

## Dependencies

**Internal (from this workspace):**
- `meditlink-commerce-common-proto` → imports generated proto classes (`ProductServiceGrpc`, `CalculatePriceRequest`, etc.)

**External Framework/Libraries:**
- Spring Boot 4.0 (Spring Framework 6.1+)
  - `spring-boot-starter-web`: REST controller support, embedded Tomcat
  - `spring-boot-starter-data-jpa`: JPA/Hibernate, transaction management
  - `spring-boot-starter-validation`: Jakarta Bean Validation
- gRPC Java client: `io.grpc:grpc-netty-shaded`, `io.grpc:grpc-protobuf`
- Jakarta Persistence (JPA): `jakarta.persistence:jakarta.persistence-api`
- Database: PostgreSQL JDBC driver (inferred from Liquibase migrations)
- Liquibase: Schema versioning and migrations

**Build Tools:**
- Gradle (see root `build.gradle.kts`)
- Protocol Buffer compiler (gradle protobuf plugin, generates Java stubs from `.proto`)

---

## Common Patterns

### REST → Orchestration → gRPC → Protocol Buffer

```
ProductBffController.create(HttpRequest)
  → ProductOrchestrationService.createProduct(DTO)
    → CoreProductGrpcGateway.createProduct(DTO)
      → ProductServiceGrpc.BlockingStub.createProduct(ProtoRequest)
        → Protocol buffer serialization over gRPC wire
```

### Configuration Injection

```java
@Configuration
public class CoreGrpcClientConfig {
    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel coreServiceManagedChannel(...) { ... }

    @Bean
    public ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub(ManagedChannel channel) {
        return ProductServiceGrpc.newBlockingStub(channel);
    }
}
```

Spring injects `ManagedChannel` into the stub bean, ensuring all stubs share the same channel.

### Transactional Logging

```java
@Service
public class RequestLogService {
    @Transactional
    public void log(String requestType, String referenceId, String status, String payloadJson) {
        IntegrationRequestLogJpaEntity entity = new IntegrationRequestLogJpaEntity(...);
        integrationRequestLogRepository.save(entity);
    }
}
```

Each orchestration operation logs its outcome to the database for audit/debugging.

