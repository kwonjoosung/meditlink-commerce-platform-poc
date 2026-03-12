<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-03-06 | Updated: 2026-03-06 -->

# meditlink-commerce-common-proto Module

## Purpose

Shared gRPC protocol buffer definitions for the commerce microservices. Defines service contracts and message schemas that are used by both the `meditlink-commerce-core` (server) and `meditlink-commerce-client` (client) modules to enable typed, language-agnostic RPC communication.

**Key Responsibilities:**
- Define gRPC service interfaces (ProductService, PriceService, CouponService)
- Define protocol buffer message types for request/response payloads
- Generate Java stubs (blocking, async, etc.) from `.proto` files
- Ensure schema evolution and backward compatibility across services
- Act as the contract layer between core and integration modules

---

## Key Files

| File | Purpose | Services & Key Messages |
|------|---------|------------------------|
| `product_service.proto` | Product CRUD and catalog operations | `ProductService`: CreateProduct, GetProduct, ListProductGroups, ListProductPlans |
| `price_service.proto` | Price calculation with coupon support | `PriceService`: CalculatePrice (with coupon code, multi-currency) |
| `coupon_service.proto` | Coupon issuance and management | `CouponService`: IssueCoupon (code, discount rate, expiry) |

---

## Service Definitions

### ProductService

**RPC Methods:**
- `CreateProduct(CreateProductRequest) → CreateProductResponse`
  - Create new product with SKU, name, base price, currency
  - Returns generated `product_id`
- `GetProduct(GetProductRequest) → GetProductResponse`
  - Fetch product details by ID
  - Returns `found: bool` flag for absence
- `ListProductGroups(ListProductGroupsRequest) → ListProductGroupsResponse`
  - Enumerate all product groups (categories)
  - Returns list of `ProductGroupItem` (id, code, name)
- `ListProductPlans(ListProductPlansRequest) → ListProductPlansResponse`
  - Fetch plans associated with a product
  - Returns `found: bool` + list of `ProductPlanItem` (id, productId, groupId, planCode, planName, price, currency)

**Key Design Note:** Product groups and plans are exposed via gRPC to support client-side UI rendering of product hierarchies.

### PriceService

**RPC Methods:**
- `CalculatePrice(CalculatePriceRequest) → CalculatePriceResponse`
  - Apply coupon code (if any) and return final price
  - Handles multi-currency, discount rate application
  - Returns `found: bool`, `appliedCouponCode`, `finalPrice`

**Key Design Note:** Price calculation is separated from ProductService to enable future decoupling of pricing/promotion engine without cascading proto changes.

### CouponService

**RPC Methods:**
- `IssueCoupon(IssueCouponRequest) → IssueCouponResponse`
  - Create/register a new coupon with code, discount rate, expiry
  - Returns `issued: bool` flag (success/conflict)

---

## Message Definitions

### ProductService Messages

```proto
message CreateProductRequest {
  string sku = 1;
  string name = 2;
  int64 base_price = 3;
  string currency = 4;
}

message CreateProductResponse {
  string product_id = 1;
}

message GetProductRequest {
  string product_id = 1;
}

message GetProductResponse {
  string product_id = 1;
  string sku = 2;
  string name = 3;
  int64 base_price = 4;
  string currency = 5;
  bool found = 6;
}

message ListProductGroupsRequest {}

message ProductGroupItem {
  string id = 1;
  string code = 2;
  string name = 3;
}

message ListProductGroupsResponse {
  repeated ProductGroupItem groups = 1;
}

message ListProductPlansRequest {
  string product_id = 1;
}

message ProductPlanItem {
  string id = 1;
  string product_id = 2;
  string group_id = 3;
  string plan_code = 4;
  string plan_name = 5;
  int64 price = 6;
  string currency = 7;
}

message ListProductPlansResponse {
  bool found = 1;
  repeated ProductPlanItem plans = 2;
}
```

### PriceService Messages

```proto
message CalculatePriceRequest {
  string product_id = 1;
  string coupon_code = 2;
}

message CalculatePriceResponse {
  string product_id = 1;
  int64 final_price = 2;
  string currency = 3;
  string applied_coupon_code = 4;
  bool found = 5;
}
```

### CouponService Messages

```proto
message IssueCouponRequest {
  string code = 1;
  int32 discount_rate = 2;
  int64 expires_at_epoch_millis = 3;
}

message IssueCouponResponse {
  string code = 1;
  int32 discount_rate = 2;
  int64 expires_at_epoch_millis = 3;
  bool issued = 4;
}
```

---

## For AI Agents

### Understanding Proto Organization

1. **Three Separate Services:**
   - `ProductService`: Catalog and product group/plan queries
   - `PriceService`: Price calculation with coupon application (decoupled for future scaling)
   - `CouponService`: Coupon lifecycle (issuance, validation)

2. **Message Patterns:**
   - Request/Response pairs (e.g., `GetProductRequest` → `GetProductResponse`)
   - Presence indicators (`found: bool`) instead of errors in responses
   - Repeated fields for collections (`repeated ProductGroupItem groups`)
   - Timestamps as epoch millis (`expires_at_epoch_millis: int64`)
   - Prices as integers (cents or base units, not floats) to avoid rounding errors

3. **Code Generation:**
   - Gradle `protobuf` plugin compiles `.proto` → Java stubs
   - Generates blocking stubs (synchronous): `ProductServiceGrpc.ProductServiceBlockingStub`
   - Stubs are imported by `meditlink-commerce-client` and `meditlink-commerce-core`

### Modification Patterns

**Adding a new RPC method:**
1. Add method signature to service in `.proto` file (e.g., `rpc DeleteProduct(DeleteProductRequest) returns (DeleteProductResponse);`)
2. Define request and response message types
3. Rebuild Gradle project to regenerate Java stubs
4. Implement in core service: add to gRPC service impl
5. Update client gateway: add call in `CoreProductGrpcGateway`
6. Add REST endpoint in `ProductBffController` if client-facing

**Evolving a message:**
- Add new fields with unique field numbers (never reuse numbers, never delete old fields)
- Keep old fields for backward compatibility
- Mark deprecated fields with `[deprecated=true]` if needed
- Example: Adding `discount_type` to `IssueCouponRequest`:
  ```proto
  message IssueCouponRequest {
    string code = 1;
    int32 discount_rate = 2;
    int64 expires_at_epoch_millis = 3;
    string discount_type = 4;  // NEW: "percentage" or "fixed_amount"
  }
  ```

**Splitting a service (example: PriceService extracted from ProductService):**
1. Create new `.proto` file with extracted messages and service
2. Leave old service in place (if other clients depend on it)
3. Update clients to import new service stubs
4. Core service implements both old and new services (can delegate internally)

### Testing Concerns

- Proto definitions are not directly unit-tested; validation occurs at generation time
- Integration tests in `meditlink-commerce-core` verify that server implementations honor the contract
- Integration tests in `meditlink-commerce-client` verify that gRPC calls match the schema
- Manual: Use grpcurl (CLI tool) to test endpoints: `grpcurl -plaintext localhost:9090 commerce.v1.ProductService/GetProduct`

### Common Pitfalls

- **Reusing field numbers:** Proto field numbering is immutable; reusing causes wire format corruption
- **Floating-point prices:** Use `int64` for prices (cents or base units) to avoid precision loss
- **Missing `found` flag:** Distinguish "not found" from "error" using boolean response fields, not exception codes
- **Timestamp confusion:** Always use epoch milliseconds (not seconds or custom formats) for cross-service consistency
- **Vague message names:** Use suffixes like Request/Response for clarity (not just Input/Output)
- **Forgetting to rebuild:** After proto changes, run Gradle `build` to regenerate Java stubs

### Documentation Anchors

- Service contract pattern: Three services with focused responsibilities (Product, Price, Coupon)
- Request/Response pattern: Paired messages with matching naming
- Optional fields: Use `bool found = N` to indicate presence (proto3 doesn't have optional by default)
- Timestamp pattern: `int64 expires_at_epoch_millis` (milliseconds since epoch)
- Collection pattern: `repeated ProductGroupItem groups` for arrays/lists

---

## Package Structure

```
commerce/v1/
  ├── product_service.proto      (ProductService + ProductGroupItem, ProductPlanItem messages)
  ├── price_service.proto        (PriceService + CalculatePriceRequest/Response messages)
  └── coupon_service.proto       (CouponService + IssueCouponRequest/Response messages)
```

Java package (from proto option): `com.meditlink.poc.commerce.common.proto.v1`

---

## Generation & Build

**Gradle Protobuf Plugin (in parent `build.gradle.kts`):**
- Invokes `protoc` (protocol buffer compiler)
- Generates Java stubs in `build/generated/source/proto/main/java/`
- Output classes are compiled into jar and published to clients

**Published Artifacts:**
- JAR contains all generated `.class` files (stubs) for import by core and client modules
- No source `.proto` files shipped in JAR; only bytecode

---

## Dependencies

**Build-Time:**
- Protocol Buffer compiler (`protoc`): Install via Gradle plugin or system package
- Gradle protobuf plugin: Configured in root `build.gradle.kts`

**Runtime (for generated code):**
- `com.google.protobuf:protobuf-java`: Runtime library for proto message classes
- `io.grpc:grpc-stub`: gRPC stub base classes
- `io.grpc:grpc-protobuf`: gRPC + protobuf integration

**Consumers:**
- `meditlink-commerce-core`: Implements services, uses generated stubs for server
- `meditlink-commerce-client`: Uses generated stubs for client calls via `CoreGrpcClientConfig`

---

## Schema Versioning

**Current Version:** `v1` (in package `commerce.v1` and Java package path)

**Evolution Strategy:**
- Minor additions (new RPC methods, new message fields) stay in `v1`
- Breaking changes (field removal, service removal) would require `v2` proto package
- Dual-serve if needed: Both `v1` and `v2` services in core, clients gradually migrate

---

## Common Patterns

### Service Separation Rationale

```
ProductService        — Catalog queries (low-latency, read-heavy)
PriceService         — Calculation engine (may scale independently, async pricing)
CouponService        — Coupon CRUD (separate concern, potential external provider)
```

This separation allows independent scaling and future service decomposition without breaking the other services.

### Request/Response Pattern

```proto
rpc GetProduct(GetProductRequest) returns (GetProductResponse);

message GetProductRequest {
  string product_id = 1;
}

message GetProductResponse {
  string product_id = 1;
  string sku = 2;
  ...
  bool found = 6;  // Indicate absence without throwing exception
}
```

Clients check `found` flag; if false, treat as "not found" rather than RPC error.

### Timestamp Standardization

```proto
message IssueCouponRequest {
  ...
  int64 expires_at_epoch_millis = 3;  // Millis since Unix epoch
}
```

Java mapping: `Instant.ofEpochMilli(expires_at_epoch_millis)` / `instant.toEpochMilli()`

