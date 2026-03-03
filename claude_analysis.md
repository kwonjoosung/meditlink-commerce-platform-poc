# meditlink-commerce-platform-poc 프로젝트 상세 분석

## 1. 프로젝트 개요

| 항목 | 내용 |
|---|---|
| 프로젝트명 | `meditlink-commerce-platform-poc` |
| 그룹 | `com.meditlink.poc.commerce` |
| 버전 | `0.0.1-SNAPSHOT` |
| 성격 | Java/Spring 기반 커머스 PoC 멀티모듈 스켈레톤 |
| JDK | **25** (Toolchain 고정) |
| 빌드 도구 | Gradle (Kotlin DSL) + Version Catalog |

---

## 2. 기술 스택

| 카테고리 | 기술 | 버전 |
|---|---|---|
| Framework | Spring Boot | **4.0.0** |
| Modulith | Spring Modulith | **2.0.0** |
| 빌드 | Gradle + Kotlin DSL | - |
| 의존성 BOM | spring-dependency-management | 1.1.7 |
| gRPC | grpc-java (Netty Shaded) | **1.76.0** |
| Protocol Buffers | protobuf-java | **4.32.0** |
| Proto 빌드 플러그인 | com.google.protobuf | 0.9.5 |
| ORM | Spring Data JPA (Hibernate) | Spring Boot 관리 |
| DB | PostgreSQL | 42.7.7 (JDBC) |
| 마이그레이션 | Liquibase | Spring Boot 관리 |
| 검증 | Jakarta Bean Validation | Spring Boot 관리 |
| 모니터링 | Spring Boot Actuator | Spring Boot 관리 |
| 테스트 | JUnit 5 (Platform) | Spring Boot 관리 |

---

## 3. 멀티모듈 구조

```
meditlink-commerce-platform-poc (root)
├── meditlink-commerce-common            ← gRPC proto 계약 + 생성 스텁 공유
├── meditlink-commerce-core-service      ← 도메인 핵심 (Hexagonal + Modulith)
└── meditlink-commerce-integration-layer ← BFF/오케스트레이션/관리자/웹훅
```

### 모듈 간 의존 관계

```
common ◄─── core-service
common ◄─── integration-layer

core-service ──gRPC(9090)──► integration-layer (blocking stub)
```

- `core-service`와 `integration-layer`는 모두 `common` 모듈에 의존
- 런타임 통신은 **gRPC** (localhost:9090, plaintext)
- `integration-layer`는 `core-service`의 코드를 직접 참조하지 않음 (proto 계약만 공유)

---

## 4. 모듈별 상세 분석

### 4.1 meditlink-commerce-common

**역할:** gRPC 서비스 계약(proto) 정의 및 Java 스텁 자동 생성

#### Proto 서비스 정의

| 서비스 | RPC 메서드 | 설명 |
|---|---|---|
| `ProductService` | `CreateProduct`, `GetProduct`, `ListProductGroups`, `ListProductPlans` | 상품 CRUD + 상품군/플랜 조회 |
| `PriceService` | `CalculatePrice` | 쿠폰 적용 포함 가격 산출 |
| `CouponService` | `IssueCoupon` | 쿠폰 발급 |

#### 주요 메시지 구조

**ProductService:**
```protobuf
CreateProductRequest  { sku, name, base_price(int64), currency }
CreateProductResponse { product_id }
GetProductRequest     { product_id }
GetProductResponse    { product_id, sku, name, base_price, currency, found }
ListProductGroupsRequest  {}
ListProductGroupsResponse { items: [ProductGroupItem{id, code, name}] }
ListProductPlansRequest   { product_id }
ListProductPlansResponse  { items: [ProductPlanItem{id, product_id, group_id, plan_code, plan_name, price, currency}] }
```

**PriceService:**
```protobuf
CalculatePriceRequest  { product_id, coupon_code }
CalculatePriceResponse { product_id, final_price(int64), currency, applied_coupon_code, found }
```

**CouponService:**
```protobuf
IssueCouponRequest  { code, discount_rate(int32), expires_at_epoch_millis(int64) }
IssueCouponResponse { code, discount_rate, expires_at_epoch_millis, issued }
```

#### 빌드 설정
- `java-library` + `protobuf` 플러그인
- protoc 4.32.0, grpc-java 1.76.0 코드 제너레이터
- 의존성: `protobuf-java`, `grpc-protobuf`, `grpc-stub`, `javax.annotation-api` (모두 `api` 스코프)

---

### 4.2 meditlink-commerce-core-service

**역할:** 도메인 핵심 서비스. Hexagonal Architecture + Spring Modulith로 모듈 경계 관리

| 속성 | 값 |
|---|---|
| HTTP 포트 | 8081 |
| gRPC 포트 | 9090 |
| DB | `jdbc:postgresql://localhost:5432/meditlink_commerce` |
| DDL 전략 | `validate` (Liquibase로 스키마 관리) |

#### Spring Modulith 모듈 구조

```
com.meditlink.poc.commerce.core
├── config/                    ← GrpcServerLifecycle (SmartLifecycle)
├── product/                   ← [Modulith Module: "Product"]
│   ├── domain/               ← NamedInterface "domain"
│   ├── application/
│   │   ├── port/in/          ← CreateProductUseCase, GetProductUseCase
│   │   ├── port/out/         ← NamedInterface "port-out" (LoadProductPort, SaveProductPort, etc.)
│   │   └── service/          ← ProductService
│   └── adapter/
│       ├── in/web/           ← ProductCommandController
│       ├── in/grpc/          ← ProductGrpcEndpoint
│       └── out/persistence/  ← JPA Entity + Repository + Adapter
├── catalog/                   ← [Modulith Module: "Catalog"] allowedDeps: product::port-out, product::domain
│   ├── domain/               ← CatalogItem
│   ├── application/          ← ListCatalogUseCase, CatalogService
│   └── adapter/in/web/       ← CatalogQueryController
├── price/                     ← [Modulith Module: "Price"] allowedDeps: product::port-out, product::domain
│   ├── domain/               ← PriceQuote
│   ├── application/          ← CalculatePriceUseCase, PriceService
│   └── adapter/in/           ← PriceQueryController, PriceGrpcEndpoint
└── coupon/                    ← [Modulith Module: "Coupon"] (독립, 의존성 없음)
    ├── domain/               ← Coupon
    ├── application/          ← IssueCouponUseCase, IssueCouponCommand, CouponService
    └── adapter/in/           ← CouponCommandController, CouponGrpcEndpoint
```

#### 모듈 간 의존 규칙 (Spring Modulith 강제)

| 모듈 | 허용된 의존 | 의미 |
|---|---|---|
| `product` | 없음 (피의존) | 다른 모듈의 핵심 제공자 |
| `catalog` | `product::port-out`, `product::domain` | Product의 출력 포트와 도메인만 접근 가능 |
| `price` | `product::port-out`, `product::domain` | 동일 |
| `coupon` | 없음 (완전 독립) | 어떤 모듈도 참조하지 않음 |

`ModulithStructureTest.verifiesModuleStructure()`에서 CI 시점 검증.

#### 도메인 모델 (Java Records)

| 도메인 객체 | 필드 | 비고 |
|---|---|---|
| `Product` | `id(Long)`, `sku`, `name`, `basePrice(long)`, `currency` | Aggregate Root, factory method `newProduct()` |
| `ProductGroup` | `id(Long)`, `code`, `name` | 상품군 분류 (소스 누락, class만 존재) |
| `ProductPlan` | `id(Long)`, `productId`, `groupId`, `planCode`, `planName`, `price(long)`, `currency` | 상품별 플랜 (소스 누락) |
| `CatalogItem` | `productId(Long)`, `sku`, `name` | 읽기 전용 프로젝션 |
| `PriceQuote` | `productId(Long)`, `finalPrice(long)`, `currency`, `appliedCouponCode` | 계산된 값 객체 |
| `Coupon` | `code`, `discountRate(int)`, `expiresAt(Instant)` | 값 객체 |

> 가격은 모두 `long` (소수 단위, 예: 원/센트). 통화 기본값 "KRW".

#### Hexagonal 아키텍처 흐름

```
[REST Controller / gRPC Endpoint]     ← Inbound Adapter
           │
           ▼
     [UseCase Interface]              ← Inbound Port
           │
           ▼
     [Application Service]            ← 비즈니스 로직
           │
           ▼
     [Port Interface]                 ← Outbound Port
           │
           ▼
[JPA Persistence Adapter]             ← Outbound Adapter
           │
           ▼
     [PostgreSQL]
```

#### Inbound Adapters (REST API)

| 엔드포인트 | 메서드 | 컨트롤러 |
|---|---|---|
| `POST /api/products` | 상품 생성 | `ProductCommandController` |
| `GET /api/products/{id}` | 상품 조회 | `ProductCommandController` |
| `GET /api/catalog/items` | 카탈로그 목록 | `CatalogQueryController` |
| `GET /api/prices/{productId}?couponCode=` | 가격 산출 | `PriceQueryController` |
| `POST /api/coupons` | 쿠폰 발급 | `CouponCommandController` |

#### Inbound Adapters (gRPC)

| 서비스 | 엔드포인트 클래스 | 메서드 |
|---|---|---|
| ProductService | `ProductGrpcEndpoint` | `createProduct`, `getProduct` |
| PriceService | `PriceGrpcEndpoint` | `calculatePrice` |
| CouponService | `CouponGrpcEndpoint` | `issueCoupon` |

gRPC 서버는 `GrpcServerLifecycle` (SmartLifecycle, phase=MAX_VALUE)에서 Netty 기반 수동 구동. `BindableService` 빈 자동 수집.

#### Outbound Adapters (Persistence)

| 어댑터 | 구현 포트 | JPA Entity | 테이블 |
|---|---|---|---|
| `ProductPersistenceAdapter` | `SaveProductPort`, `LoadProductPort` | `ProductJpaEntity` | `products` |
| `ProductGroupPersistenceAdapter` | `LoadProductGroupPort` | `ProductGroupJpaEntity` | `product_groups` |
| `ProductPlanPersistenceAdapter` | `LoadProductPlanPort` | `ProductPlanJpaEntity` | `product_plans` |

#### 데이터베이스 스키마 (Liquibase)

**0001-initial-product-domain.yaml** (src에 포함):

| 테이블 | 주요 컬럼 | 비고 |
|---|---|---|
| `products` | `id(PK)`, `sku(UNIQUE)`, `name`, `base_price(BIGINT)`, `currency(VARCHAR3)` | 상품 마스터 |
| `catalog_items` | `id(PK)`, `product_id(FK→products)`, `display_name` | 카탈로그 항목 |
| `price_policies` | `id(PK)`, `product_id(FK→products)`, `policy_name`, `discount_rate` | 가격 정책 (**미사용**) |
| `coupons` | `id(PK)`, `code(UNIQUE)`, `discount_rate`, `expires_at` | 쿠폰 (**미사용**) |

**0002-product-group-plan.yaml** (build/resources에만 존재, src 미포함):

| 테이블 | 주요 컬럼 | 비고 |
|---|---|---|
| `product_groups` | `id(PK)`, `code(UNIQUE)`, `name` | 상품군 |
| `product_plans` | `id(PK)`, `product_id(FK)`, `group_id(FK)`, `plan_code`, `plan_name`, `price`, `currency` | 상품 플랜 |

---

### 4.3 meditlink-commerce-integration-layer

**역할:** 외부 연동 계층. BFF, 오케스트레이션, 관리자, 웹훅을 단일 앱에 서브패키지로 구성

| 속성 | 값 |
|---|---|
| HTTP 포트 | 8080 |
| gRPC 대상 | `localhost:9090` (core-service) |
| DB | `jdbc:postgresql://localhost:5432/meditlink_integration` |
| DDL 전략 | `validate` |

#### 패키지 구조

```
com.meditlink.poc.commerce.integration
├── IntegrationLayerApplication.java    ← @SpringBootApplication
├── bff/web/
│   └── ProductBffController.java       ← 소비자 BFF API
├── admin/web/
│   └── AdminProductController.java     ← 관리자 API
├── webhook/web/
│   └── ProductWebhookController.java   ← 웹훅 수신 (스텁)
├── orchestration/
│   ├── ProductOrchestrationService.java ← 오케스트레이션 (gateway + 감사 로깅)
│   └── dto/
│       ├── CreateProductHttpRequest.java
│       ├── ProductHttpResponse.java
│       ├── PriceQuoteHttpResponse.java
│       ├── IssueCouponHttpRequest.java
│       └── IssueCouponHttpResponse.java
├── gateway/grpc/
│   └── CoreProductGrpcGateway.java     ← Anti-Corruption Layer (DTO↔Proto 변환)
├── config/
│   └── CoreGrpcClientConfig.java       ← ManagedChannel + 3 BlockingStub Bean
└── state/
    ├── IntegrationRequestLogJpaEntity.java
    ├── IntegrationRequestLogRepository.java
    └── RequestLogService.java          ← 감사 로그 저장
```

#### 계층 흐름

```
HTTP Client
    │
    ▼
[BFF / Admin / Webhook Controller]   ← Web Layer (REST)
    │
    ▼
[ProductOrchestrationService]         ← 오케스트레이션 (gateway 호출 + 감사 로깅)
    │                   │
    ▼                   ▼
[CoreProductGrpcGateway]     [RequestLogService]
    │ (blocking stub)          │ (@Transactional)
    ▼                          ▼
[core-service:9090]     [PostgreSQL: integration_request_logs]
```

#### REST API 엔드포인트

**BFF (`/api/bff`)**

| 메서드 | 경로 | 요청 | 응답 | 상태 |
|---|---|---|---|---|
| POST | `/api/bff/products` | `CreateProductHttpRequest` | `CreateProductBffResponse{productId}` | 201 |
| GET | `/api/bff/products/{productId}` | - | `ProductHttpResponse` | 200/404 |
| GET | `/api/bff/products/{productId}/price?couponCode=` | - | `PriceQuoteHttpResponse` | 200/404 |
| POST | `/api/bff/coupons` | `IssueCouponHttpRequest` | `IssueCouponHttpResponse` | 201 |

**Admin (`/api/admin`)**

| 메서드 | 경로 | 응답 | 상태 |
|---|---|---|---|
| GET | `/api/admin/products/{productId}` | `ProductHttpResponse` | 200/404 |

**Webhook (`/api/webhooks`)**

| 메서드 | 경로 | 요청 | 응답 | 상태 |
|---|---|---|---|---|
| POST | `/api/webhooks/products/events` | `ProductWebhookEvent{eventType, payload}` | `WebhookAcceptedResponse` | 202 |

#### gRPC 클라이언트 설정

- `ManagedChannel`: plaintext, `ManagedChannelBuilder.forAddress(host, port).usePlaintext()`
- `destroyMethod = "shutdownNow"` 으로 정리
- 3개 Blocking Stub 빈: `ProductServiceBlockingStub`, `PriceServiceBlockingStub`, `CouponServiceBlockingStub`

#### 감사 로그 테이블

```sql
CREATE TABLE integration_request_logs (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_type VARCHAR(50) NOT NULL,     -- CREATE_PRODUCT, GET_PRODUCT, CALCULATE_PRICE, ISSUE_COUPON
    reference_id VARCHAR(100),             -- productId or couponCode
    status       VARCHAR(30) NOT NULL,     -- SUCCESS, NOT_FOUND, FAILED
    payload_json TEXT,
    created_at   TIMESTAMP NOT NULL
);
INDEX idx_integration_request_logs_type_created_at (request_type, created_at)
```

---

## 5. 전체 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────────────┐
│           meditlink-commerce-integration-layer (:8080)       │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌───────────┐   │
│  │ BFF API  │  │Admin API │  │Webhook   │  │ Actuator  │   │
│  │/api/bff  │  │/api/admin│  │/api/      │  │/actuator  │   │
│  └────┬─────┘  └────┬─────┘  │webhooks  │  └───────────┘   │
│       │              │        └──────────┘                   │
│       └──────┬───────┘                                       │
│              ▼                                               │
│   ┌──────────────────────┐    ┌──────────────────┐          │
│   │ Orchestration Service│───►│ RequestLogService│          │
│   └──────────┬───────────┘    └────────┬─────────┘          │
│              ▼                          ▼                     │
│   ┌──────────────────────┐    ┌──────────────────┐          │
│   │  gRPC Gateway (ACL)  │    │PostgreSQL        │          │
│   │  - ProductStub       │    │meditlink_        │          │
│   │  - PriceStub         │    │integration       │          │
│   │  - CouponStub        │    └──────────────────┘          │
│   └──────────┬───────────┘                                   │
│              │ gRPC (plaintext)                              │
└──────────────┼──────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────┐
│           meditlink-commerce-core-service (:8081 / :9090)    │
│                                                              │
│  ┌─────────────────────────────────────────────────┐        │
│  │ gRPC Server (Netty, SmartLifecycle)             │        │
│  │  ProductGrpcEndpoint                            │        │
│  │  PriceGrpcEndpoint                              │        │
│  │  CouponGrpcEndpoint                             │        │
│  └──────────────────┬──────────────────────────────┘        │
│                     │                                        │
│  ┌──────────────────▼──────────────────────────────┐        │
│  │ REST Controllers (:8081)                         │        │
│  │  /api/products  /api/catalog  /api/prices        │        │
│  │  /api/coupons                                    │        │
│  └──────────────────┬──────────────────────────────┘        │
│                     │                                        │
│  ┌──────────────────▼──────────────────────────────┐        │
│  │ Application Services (Use Cases)                 │        │
│  │  ┌──────────┐ ┌──────────┐ ┌───────┐ ┌───────┐ │        │
│  │  │ Product  │ │ Catalog  │ │ Price │ │Coupon │ │        │
│  │  │ Service  │ │ Service  │ │Service│ │Service│ │        │
│  │  └────┬─────┘ └────┬─────┘ └───┬───┘ └───────┘ │        │
│  │       │             │           │                │        │
│  │  ┌────▼─────────────▼───────────▼──────┐        │        │
│  │  │ Outbound Ports (port-out interface)  │        │        │
│  │  │  LoadProductPort, SaveProductPort    │        │        │
│  │  │  LoadProductGroupPort                │        │        │
│  │  │  LoadProductPlanPort                 │        │        │
│  │  └────────────────┬────────────────────┘        │        │
│  └───────────────────┼────────────────────────────┘        │
│                      ▼                                       │
│  ┌───────────────────────────────────────────────┐          │
│  │ JPA Persistence Adapters                       │          │
│  │  ProductPersistenceAdapter                    │          │
│  │  ProductGroupPersistenceAdapter               │          │
│  │  ProductPlanPersistenceAdapter                │          │
│  └───────────────────┬───────────────────────────┘          │
│                      ▼                                       │
│  ┌───────────────────────────────────────────────┐          │
│  │ PostgreSQL: meditlink_commerce                 │          │
│  │  products, catalog_items, price_policies,     │          │
│  │  coupons, product_groups, product_plans       │          │
│  └───────────────────────────────────────────────┘          │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│              meditlink-commerce-common                        │
│  Proto 계약: ProductService, PriceService, CouponService     │
│  생성 스텁: *Grpc.java, *Request.java, *Response.java         │
│  (두 서비스 모듈에서 공유)                                      │
└──────────────────────────────────────────────────────────────┘
```

---

## 6. 데이터베이스 전체 스키마

### core-service DB (`meditlink_commerce`)

| 테이블 | PK | 주요 컬럼 | FK/인덱스 |
|---|---|---|---|
| `products` | `id` | `sku(UQ)`, `name`, `base_price`, `currency` | - |
| `catalog_items` | `id` | `product_id`, `display_name` | FK→products |
| `price_policies` | `id` | `product_id`, `policy_name`, `discount_rate` | FK→products |
| `coupons` | `id` | `code(UQ)`, `discount_rate`, `expires_at` | - |
| `product_groups` | `id` | `code(UQ)`, `name` | - |
| `product_plans` | `id` | `product_id`, `group_id`, `plan_code`, `plan_name`, `price`, `currency` | FK→products, FK→product_groups, IDX(product_id) |

### integration-layer DB (`meditlink_integration`)

| 테이블 | PK | 주요 컬럼 | 인덱스 |
|---|---|---|---|
| `integration_request_logs` | `id` | `request_type`, `reference_id`, `status`, `payload_json`, `created_at` | IDX(request_type, created_at) |

---

## 7. 실행 순서 및 포트 정리

```
1. PostgreSQL 실행
     ├── meditlink_commerce DB 생성
     └── meditlink_integration DB 생성

2. core-service 실행
     ├── Liquibase 마이그레이션 실행
     ├── HTTP REST :8081
     └── gRPC Netty :9090

3. integration-layer 실행
     ├── Liquibase 마이그레이션 실행
     ├── HTTP REST :8080
     └── gRPC Client → localhost:9090
```

| 서비스 | HTTP | gRPC | DB |
|---|---|---|---|
| core-service | 8081 | 9090 (server) | meditlink_commerce |
| integration-layer | 8080 | 9090 (client) | meditlink_integration |

---

## 8. 주요 설계 패턴 및 특징

### 8.1 Hexagonal Architecture (Ports & Adapters)
- **Inbound Port**: UseCase 인터페이스 (`CreateProductUseCase`, `GetProductUseCase` 등)
- **Outbound Port**: Repository 추상화 (`LoadProductPort`, `SaveProductPort` 등)
- **Inbound Adapter**: REST Controller + gRPC Endpoint (이중 전송 계층)
- **Outbound Adapter**: JPA Persistence Adapter

### 8.2 Spring Modulith
- `package-info.java`에서 `@ApplicationModule` 선언
- `@NamedInterface`로 모듈 간 공개 인터페이스 제한 (`domain`, `port-out`)
- `ModulithStructureTest`로 CI 시점 아키텍처 규칙 검증

### 8.3 Anti-Corruption Layer (ACL)
- `CoreProductGrpcGateway`가 Protobuf ↔ HTTP DTO 변환 전담
- Protobuf 타입이 gateway 밖으로 유출되지 않음

### 8.4 CQRS 경향
- Command: `ProductCommandController` (POST)
- Query: `CatalogQueryController`, `PriceQueryController` (GET)
- 완전한 CQRS는 아니지만, 커맨드/쿼리 분리 패턴 적용

### 8.5 감사 로깅
- `ProductOrchestrationService`에서 모든 외부 호출에 대해 `integration_request_logs` 기록
- `requestType`, `referenceId`, `status` 기반 추적

---

## 9. 식별된 이슈 및 개선 필요사항

### 9.1 소스 파일 누락 (Critical)
다음 클래스들이 빌드 출력(`.class`)에는 존재하지만 `src/` 디렉토리에 `.java` 소스가 없음:
- `ProductGroup` (domain record)
- `ProductPlan` (domain record)
- `ListProductGroupUseCase` (inbound port)
- `ListProductPlanUseCase` (inbound port)
- `ProductGroupPlanQueryService` (application service)
- `ProductGroupPlanQueryController` (REST adapter)

### 9.2 Liquibase 마이그레이션 누락
- `0002-product-group-plan.yaml`이 `build/resources/`에만 존재하고 `src/main/resources/`에 없음
- `db.changelog-master.yaml`에서 `0002` include가 빠져있음
- 실제 빌드 시 `product_groups`, `product_plans` 테이블이 생성되지 않을 가능성

### 9.3 미사용 테이블/기능
| 항목 | 상태 |
|---|---|
| `price_policies` 테이블 | DDL 생성되지만 어떤 코드에서도 사용하지 않음 |
| `coupons` 테이블 | DDL 생성되지만 `CouponService`가 인메모리만 사용 |
| `catalog_items` 테이블 | DDL 생성되지만 `CatalogService`가 products 테이블에서 직접 조회 |

### 9.4 하드코딩된 비즈니스 로직
- `PriceService`: 쿠폰 코드가 `"SALE10"`으로 시작하면 10% 할인 — 하드코딩
- `CouponService`: 영속성 없이 인메모리 처리, 발급된 쿠폰을 저장하지 않음

### 9.5 보안 미적용
- Spring Security 미설정 (README에도 명시)
- gRPC 통신 plaintext (TLS 미적용)
- 인증/인가 없음

### 9.6 gRPC 서버 수동 관리
- Spring Boot gRPC Starter 미사용, `SmartLifecycle`으로 수동 관리
- 서비스 등록, 헬스체크, 인터셉터 등을 직접 구현해야 함

### 9.7 Webhook 스텁
- `ProductWebhookController`는 이벤트 수신만 하고 처리 로직 없음 (202 응답만 반환)

---

## 10. 파일 목록 전체

### common (소스만)
```
build.gradle.kts
src/main/proto/commerce/v1/product_service.proto
src/main/proto/commerce/v1/price_service.proto
src/main/proto/commerce/v1/coupon_service.proto
```

### core-service (소스만)
```
build.gradle.kts
src/main/resources/application.yml
src/main/resources/db/changelog/db.changelog-master.yaml
src/main/resources/db/changelog/changes/0001-initial-product-domain.yaml
src/main/java/.../core/CoreServiceApplication.java
src/main/java/.../core/config/GrpcServerLifecycle.java
src/main/java/.../core/product/package-info.java
src/main/java/.../core/product/domain/package-info.java
src/main/java/.../core/product/domain/Product.java
src/main/java/.../core/product/application/port/in/CreateProductCommand.java
src/main/java/.../core/product/application/port/in/CreateProductUseCase.java
src/main/java/.../core/product/application/port/in/GetProductUseCase.java
src/main/java/.../core/product/application/port/out/package-info.java
src/main/java/.../core/product/application/port/out/LoadProductPort.java
src/main/java/.../core/product/application/port/out/SaveProductPort.java
src/main/java/.../core/product/application/port/out/LoadProductGroupPort.java
src/main/java/.../core/product/application/port/out/LoadProductPlanPort.java
src/main/java/.../core/product/application/service/ProductService.java
src/main/java/.../core/product/adapter/in/web/ProductCommandController.java
src/main/java/.../core/product/adapter/in/grpc/ProductGrpcEndpoint.java
src/main/java/.../core/product/adapter/out/persistence/ProductJpaEntity.java
src/main/java/.../core/product/adapter/out/persistence/ProductJpaRepository.java
src/main/java/.../core/product/adapter/out/persistence/ProductPersistenceAdapter.java
src/main/java/.../core/product/adapter/out/persistence/ProductGroupJpaEntity.java
src/main/java/.../core/product/adapter/out/persistence/ProductGroupJpaRepository.java
src/main/java/.../core/product/adapter/out/persistence/ProductGroupPersistenceAdapter.java
src/main/java/.../core/product/adapter/out/persistence/ProductPlanJpaEntity.java
src/main/java/.../core/product/adapter/out/persistence/ProductPlanJpaRepository.java
src/main/java/.../core/product/adapter/out/persistence/ProductPlanPersistenceAdapter.java
src/main/java/.../core/catalog/package-info.java
src/main/java/.../core/catalog/domain/CatalogItem.java
src/main/java/.../core/catalog/application/port/in/ListCatalogUseCase.java
src/main/java/.../core/catalog/application/service/CatalogService.java
src/main/java/.../core/catalog/adapter/in/web/CatalogQueryController.java
src/main/java/.../core/price/package-info.java
src/main/java/.../core/price/domain/PriceQuote.java
src/main/java/.../core/price/application/port/in/CalculatePriceUseCase.java
src/main/java/.../core/price/application/service/PriceService.java
src/main/java/.../core/price/adapter/in/web/PriceQueryController.java
src/main/java/.../core/price/adapter/in/grpc/PriceGrpcEndpoint.java
src/main/java/.../core/coupon/package-info.java
src/main/java/.../core/coupon/domain/Coupon.java
src/main/java/.../core/coupon/application/port/in/IssueCouponCommand.java
src/main/java/.../core/coupon/application/port/in/IssueCouponUseCase.java
src/main/java/.../core/coupon/application/service/CouponService.java
src/main/java/.../core/coupon/adapter/in/web/CouponCommandController.java
src/main/java/.../core/coupon/adapter/in/grpc/CouponGrpcEndpoint.java
src/test/java/.../core/ModulithStructureTest.java
```

### integration-layer (소스만)
```
build.gradle.kts
src/main/resources/application.yml
src/main/resources/db/changelog/db.changelog-master.yaml
src/main/resources/db/changelog/changes/0001-integration-request-logs.yaml
src/main/java/.../integration/IntegrationLayerApplication.java
src/main/java/.../integration/bff/web/ProductBffController.java
src/main/java/.../integration/admin/web/AdminProductController.java
src/main/java/.../integration/webhook/web/ProductWebhookController.java
src/main/java/.../integration/orchestration/ProductOrchestrationService.java
src/main/java/.../integration/orchestration/dto/CreateProductHttpRequest.java
src/main/java/.../integration/orchestration/dto/ProductHttpResponse.java
src/main/java/.../integration/orchestration/dto/PriceQuoteHttpResponse.java
src/main/java/.../integration/orchestration/dto/IssueCouponHttpRequest.java
src/main/java/.../integration/orchestration/dto/IssueCouponHttpResponse.java
src/main/java/.../integration/gateway/grpc/CoreProductGrpcGateway.java
src/main/java/.../integration/config/CoreGrpcClientConfig.java
src/main/java/.../integration/state/IntegrationRequestLogJpaEntity.java
src/main/java/.../integration/state/IntegrationRequestLogRepository.java
src/main/java/.../integration/state/RequestLogService.java
```

---

## 11. 요약

이 프로젝트는 **Spring Boot 4.0 + Spring Modulith + gRPC + Hexagonal Architecture** 조합의 커머스 PoC입니다. 3개 Gradle 모듈로 구성되며, proto 계약 공유(common) → 도메인 핵심(core-service) → 외부 연동(integration-layer)의 계층 구조를 갖습니다.

**핵심 강점:**
- Hexagonal + Modulith 조합으로 명확한 모듈 경계와 의존성 제어
- gRPC를 통한 서비스 간 계약 기반 통신
- 이중 전송 계층 (REST + gRPC) 제공
- Liquibase 기반 스키마 관리 (DDL validate 전략)

**PoC 단계 한계:**
- 소스 파일 일부 누락 (ProductGroup/Plan 관련)
- Liquibase 0002 마이그레이션 미연결
- 일부 테이블 미사용 (price_policies, coupons, catalog_items)
- 보안/인증 미적용
- 쿠폰/가격 로직 하드코딩
- 웹훅 스텁 상태
