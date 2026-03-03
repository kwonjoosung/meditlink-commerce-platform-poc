# meditlink-commerce-platform-poc 상세 분석

> 분석 시점: 2026-03-03
> 대상 브랜치: `claude` (base: `agent/codex/test-1`)
> 워크스페이스 루트: `commerce_test`

---

## 1. 프로젝트 개요

Java/Spring 기반 커머스 PoC 멀티모듈 프로젝트로, **계약 분리(common) + 도메인 중심(core) + 대외 연동(integration)** 3계층 아키텍처를 구현한다.

- **목적**: Spring Boot 4.x + Spring Modulith + Hexagonal Architecture + gRPC 조합의 기술 검증
- **패키지 베이스**: `com.meditlink.poc.commerce`
- **JDK**: 25 (Toolchain 고정)

---

## 2. 기술 스택 상세

| 구분 | 기술 | 버전 |
|------|------|------|
| 빌드 도구 | Gradle (Kotlin DSL) | 9.0.0 |
| 언어 | Java | 25 |
| 프레임워크 | Spring Boot | 4.0.0 |
| 모듈 관리 | Spring Modulith | 2.0.0 |
| RPC | gRPC (netty-shaded) | 1.76.0 |
| 직렬화 | Protocol Buffers | 4.32.0 |
| ORM | Spring Data JPA + Hibernate | Spring Boot BOM |
| DB | PostgreSQL | 42.7.7 (드라이버) |
| 마이그레이션 | Liquibase | Spring Boot BOM |
| 검증 | Jakarta Validation | Spring Boot BOM |
| 모니터링 | Spring Actuator | Spring Boot BOM |

### 빌드 설정 특이사항
- `gradle/libs.versions.toml`로 중앙 버전 카탈로그 관리
- `dependencyResolutionManagement`에서 `FAIL_ON_PROJECT_REPOS` 설정으로 서브프로젝트 리포지토리 추가 차단
- `javax.annotation-api:1.3.2` 의존성이 gRPC 생성 코드용으로 남아있음 (Jakarta 전환 미완료 지점)

---

## 3. 멀티모듈 구조

```
meditlink-commerce-platform-poc (root)
├── settings.gradle.kts          # 3개 서브모듈 선언
├── build.gradle.kts             # allprojects: Java 25 toolchain, JUnit Platform
├── gradle/libs.versions.toml    # 중앙 버전 카탈로그
│
├── meditlink-commerce-common/           # 모듈 1: 계약 공유
│   ├── build.gradle.kts                 # java-library + protobuf 플러그인
│   └── src/main/proto/commerce/v1/      # .proto 파일 3개
│
├── meditlink-commerce-core-service/     # 모듈 2: 핵심 도메인
│   ├── build.gradle.kts                 # spring-boot + spring-modulith
│   └── src/
│       ├── main/java/                   # 도메인 모듈 4개
│       ├── main/resources/              # application.yml + Liquibase
│       └── test/java/                   # ModulithStructureTest
│
└── meditlink-commerce-integration-layer/ # 모듈 3: 대외 연동
    ├── build.gradle.kts                  # spring-boot + grpc client
    └── src/
        ├── main/java/                    # BFF/Admin/Orchestration/Gateway/Webhook/State
        └── main/resources/               # application.yml + Liquibase
```

### 모듈 간 의존 관계

```
common <── core-service
common <── integration-layer
(core-service와 integration-layer 간에는 직접 코드 의존 없음, gRPC 통신만 존재)
```

---

## 4. 모듈 1: meditlink-commerce-common (계약 공유)

### 역할
gRPC `.proto` 계약 정의 및 Java/gRPC stub 코드 자동 생성. Core와 Integration이 동일 계약 타입에 의존하도록 결합점을 단일화한다.

### Proto 서비스 정의

#### 4.1 ProductService (`product_service.proto`)
```protobuf
service ProductService {
  rpc CreateProduct(CreateProductRequest) returns (CreateProductResponse);
  rpc GetProduct(GetProductRequest) returns (GetProductResponse);
}
```

| 메시지 | 필드 |
|--------|------|
| `CreateProductRequest` | sku, name, base_price(int64), currency |
| `CreateProductResponse` | product_id(string) |
| `GetProductRequest` | product_id(string) |
| `GetProductResponse` | product_id, sku, name, base_price, currency, found(bool) |

#### 4.2 PriceService (`price_service.proto`)
```protobuf
service PriceService {
  rpc CalculatePrice(CalculatePriceRequest) returns (CalculatePriceResponse);
}
```

| 메시지 | 필드 |
|--------|------|
| `CalculatePriceRequest` | product_id, coupon_code |
| `CalculatePriceResponse` | product_id, final_price(int64), currency, applied_coupon_code, found(bool) |

#### 4.3 CouponService (`coupon_service.proto`)
```protobuf
service CouponService {
  rpc IssueCoupon(IssueCouponRequest) returns (IssueCouponResponse);
}
```

| 메시지 | 필드 |
|--------|------|
| `IssueCouponRequest` | code, discount_rate(int32), expires_at_epoch_millis(int64) |
| `IssueCouponResponse` | code, discount_rate, expires_at_epoch_millis, issued(bool) |

### 빌드 설정
- `protoc:4.32.0` + `protoc-gen-grpc-java:1.76.0`으로 코드 생성
- `sourceSets`에 `build/generated/source/proto/main/java`와 `grpc` 디렉토리 추가
- `api()` 의존성으로 protobuf-java, grpc-protobuf, grpc-stub, javax-annotation-api를 전이적으로 공유

---

## 5. 모듈 2: meditlink-commerce-core-service (핵심 도메인)

### 5.1 Spring Modulith 도메인 모듈 구성

Core Service는 4개의 Spring Modulith 모듈로 구성된다:

| 모듈 | 패키지 | `@ApplicationModule` |
|------|--------|---------------------|
| Product | `core.product` | `displayName = "Product"` |
| Catalog | `core.catalog` | `displayName = "Catalog"` |
| Price | `core.price` | `displayName = "Price"` |
| Coupon | `core.coupon` | `displayName = "Coupon"` |

각 모듈의 `package-info.java`에 `@ApplicationModule` 어노테이션이 선언되어 있으며, `ModulithStructureTest`에서 경계 검증을 수행한다.

### 5.2 Hexagonal Architecture 레이어 분석

각 도메인 모듈은 다음 헥사고날 레이어를 따른다:

```
domain/          # 핵심 도메인 모델 (record, 순수 POJO)
application/
  port/in/       # 인바운드 포트 (UseCase 인터페이스, Command DTO)
  port/out/      # 아웃바운드 포트 (영속화/외부 시스템 인터페이스)
  service/       # 유스케이스 구현체 (포트 조합)
adapter/
  in/web/        # REST 컨트롤러 (인바운드 어댑터)
  in/grpc/       # gRPC 엔드포인트 (인바운드 어댑터)
  out/persistence/ # JPA 영속화 (아웃바운드 어댑터)
```

---

### 5.3 Product 모듈 (가장 완전한 구현)

#### 도메인 모델
```java
// Product.java - 핵심 Aggregate Root
public record Product(Long id, String sku, String name, long basePrice, String currency) {
    public static Product newProduct(String sku, String name, long basePrice, String currency) {
        return new Product(null, sku, name, basePrice, currency);
    }
}
```

- `record`로 불변성 보장
- 팩토리 메서드 `newProduct()`에서 id=null로 신규 객체 생성
- 가격은 `long` 타입 (정수 기반, 소수점 미사용)

#### 인바운드 포트 (Use Cases)

| 인터페이스 | 메서드 | 설명 |
|-----------|--------|------|
| `CreateProductUseCase` | `Product create(CreateProductCommand)` | 상품 생성 |
| `GetProductUseCase` | `Optional<Product> getById(Long)` | 상품 단건 조회 |

```java
// CreateProductCommand.java
public record CreateProductCommand(String sku, String name, long basePrice, String currency) {}
```

#### 아웃바운드 포트

| 인터페이스 | 메서드 | 설명 |
|-----------|--------|------|
| `SaveProductPort` | `Product save(Product)` | 상품 저장 |
| `LoadProductPort` | `Optional<Product> loadById(Long)` | ID로 조회 |
|                    | `List<Product> loadAll()` | 전체 조회 |
| `LoadProductGroupPort` | `List<ProductGroup> loadAllGroups()` | 상품 그룹 전체 조회 |
| `LoadProductPlanPort` | `List<ProductPlan> loadByProductId(Long)` | 상품별 플랜 조회 |

- `port.out` 패키지는 `@NamedInterface("port-out")`로 선언되어 다른 모듈(catalog, price)에서 접근 가능

#### 서비스 구현
```java
@Service
@Transactional
public class ProductService implements CreateProductUseCase, GetProductUseCase {
    // 생성자 주입: SaveProductPort, LoadProductPort

    public Product create(CreateProductCommand command) {
        // currency가 null/blank이면 "KRW" 기본값 적용
        String currency = command.currency() == null || command.currency().isBlank()
            ? "KRW" : command.currency();
        Product newProduct = Product.newProduct(command.sku(), command.name(),
            command.basePrice(), currency);
        return saveProductPort.save(newProduct);
    }

    @Transactional(readOnly = true)
    public Optional<Product> getById(Long productId) {
        return loadProductPort.loadById(productId);
    }
}
```

#### 인바운드 어댑터: REST
```java
@RestController
@RequestMapping("/api/products")
public class ProductCommandController {
    // POST /api/products - 상품 생성 (201 Created)
    // GET /api/products/{id} - 상품 조회 (200 OK / 404)

    // 내부 Request/Response record 정의
    record CreateProductRequest(@NotBlank String sku, @NotBlank String name,
                                @PositiveOrZero long basePrice, @NotBlank String currency) {}
    record ProductResponse(Long id, String sku, String name, long basePrice, String currency) {
        static ProductResponse from(Product product) { ... }
    }
}
```

#### 인바운드 어댑터: gRPC
```java
@Service
public class ProductGrpcEndpoint extends ProductServiceGrpc.ProductServiceImplBase {
    // createProduct: proto 메시지 -> CreateProductCommand -> UseCase -> proto 응답
    // getProduct: productId 파싱 -> UseCase -> found 플래그로 분기
    //   - 숫자가 아닌 productId인 경우 INVALID_ARGUMENT 에러 반환
}
```

#### 아웃바운드 어댑터: JPA 영속화
```java
@Entity @Table(name = "products")
public class ProductJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(nullable = false, unique = true) String sku;
    @Column(nullable = false) String name;
    @Column(name = "base_price", nullable = false) long basePrice;
    @Column(nullable = false, length = 3) String currency;

    public static ProductJpaEntity from(Product product) { ... }
    public Product toDomain() { ... }
}

@Component
public class ProductPersistenceAdapter implements SaveProductPort, LoadProductPort {
    // JPA Repository를 통한 도메인 변환 + CRUD
}
```

#### 추가 엔티티: ProductGroup, ProductPlan
- `ProductGroupJpaEntity`: id, code, name (상품 그룹)
- `ProductPlanJpaEntity`: id, productId, groupId, planCode, planName, price, currency (상품 플랜)
- 각각 전용 Repository + PersistenceAdapter 보유
- **주의**: 이들은 JPA 엔티티만 존재하고 도메인 record 클래스는 아직 미구현 상태

---

### 5.4 Catalog 모듈

#### 도메인 모델
```java
public record CatalogItem(Long productId, String sku, String displayName) {}
```

#### 유스케이스
```java
public interface ListCatalogUseCase {
    List<CatalogItem> listItems();
}
```

#### 서비스 구현
```java
@Service
@Transactional(readOnly = true)
public class CatalogService implements ListCatalogUseCase {
    private final LoadProductPort loadProductPort;  // product 모듈의 아웃바운드 포트 참조

    public List<CatalogItem> listItems() {
        return loadProductPort.loadAll().stream()
            .map(product -> new CatalogItem(product.id(), product.sku(), product.name()))
            .toList();
    }
}
```

- **모듈 간 의존**: Catalog -> Product (via `LoadProductPort` named interface "port-out")
- 자체 영속 계층 없음 (Product 모듈의 포트를 통해 데이터 획득)

#### REST 어댑터
- `GET /api/catalog` -> `ListCatalogUseCase.listItems()`

---

### 5.5 Price 모듈

#### 도메인 모델
```java
public record PriceQuote(Long productId, long finalPrice, String currency, String appliedCouponCode) {}
```

#### 유스케이스
```java
public interface CalculatePriceUseCase {
    PriceQuote calculate(Long productId, String couponCode);
}
```

#### 서비스 구현
```java
@Service
@Transactional(readOnly = true)
public class PriceService implements CalculatePriceUseCase {
    private final LoadProductPort loadProductPort;  // product 모듈의 포트 참조

    public PriceQuote calculate(Long productId, String couponCode) {
        Product product = loadProductPort.loadById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        long finalPrice = product.basePrice();
        if (couponCode != null && couponCode.startsWith("SALE10")) {
            finalPrice = Math.round(product.basePrice() * 0.9);  // 10% 할인 하드코딩
        }
        return new PriceQuote(product.id(), finalPrice, product.currency(), couponCode);
    }
}
```

- **비즈니스 규칙**: "SALE10"으로 시작하는 쿠폰 코드에 대해 10% 할인 (PoC 하드코딩)
- **모듈 간 의존**: Price -> Product (via `LoadProductPort`)
- 자체 영속 계층 없음

#### gRPC 어댑터
```java
@Service
public class PriceGrpcEndpoint extends PriceServiceGrpc.PriceServiceImplBase {
    // productId 파싱 실패 시 found=false 반환
    // EntityNotFoundException 발생 시 found=false 반환
    // appliedCouponCode가 null이면 빈 문자열로 변환
}
```

#### REST 어댑터
- `GET /api/prices?productId={id}&couponCode={code}` -> `CalculatePriceUseCase.calculate()`

---

### 5.6 Coupon 모듈

#### 도메인 모델
```java
public record Coupon(String code, int discountRate, java.time.Instant expiresAt) {}
```

#### 유스케이스
```java
public interface IssueCouponUseCase {
    Coupon issue(IssueCouponCommand command);
}
// IssueCouponCommand: record(String code, int discountRate, Instant expiresAt)
```

#### 서비스 구현
```java
@Service
public class CouponService implements IssueCouponUseCase {
    public Coupon issue(IssueCouponCommand command) {
        int normalizedDiscount = Math.max(0, Math.min(command.discountRate(), 100));
        return new Coupon(command.code(), normalizedDiscount, command.expiresAt());
    }
}
```

- **비즈니스 규칙**: discountRate를 0~100 범위로 정규화
- **주의**: 영속화 없음 - 쿠폰을 DB에 저장하지 않고 메모리에서 생성만 함 (DB 테이블 `coupons`는 존재하지만 JPA 엔티티/Repository 미구현)
- 독립 모듈 - 다른 모듈에 의존하지 않음

#### gRPC 어댑터
```java
@Service
public class CouponGrpcEndpoint extends CouponServiceGrpc.CouponServiceImplBase {
    // expiresAtEpochMillis <= 0이면 현재 시점 + 30일로 자동 설정
    // 항상 issued=true 반환
}
```

#### REST 어댑터
- `POST /api/coupons` -> `IssueCouponUseCase.issue()`

---

### 5.7 인프라 컴포넌트

#### GrpcServerLifecycle
```java
@Component
public class GrpcServerLifecycle implements SmartLifecycle {
    // List<BindableService>를 주입받아 모든 gRPC 서비스를 자동 등록
    // 포트: grpc.server.port (기본 9090)
    // NettyServerBuilder 사용
    // phase = Integer.MAX_VALUE (가장 마지막에 시작)
}
```

- 컬렉션 주입 패턴으로 새 gRPC 엔드포인트 추가 시 자동 등록

#### CoreServiceApplication
```java
@SpringBootApplication
public class CoreServiceApplication {
    public static void main(String[] args) { SpringApplication.run(...); }
}
```

---

### 5.8 데이터베이스 스키마 (Core)

**DB명**: `meditlink_commerce` (PostgreSQL, 포트 5432)

#### Liquibase Changelog: `0001-initial-product-domain.yaml`

| 테이블 | 컬럼 | 타입 | 제약조건 |
|--------|------|------|----------|
| **products** | id | BIGINT | PK, AUTO_INCREMENT |
|              | sku | VARCHAR(64) | NOT NULL, UNIQUE |
|              | name | VARCHAR(255) | NOT NULL |
|              | base_price | BIGINT | NOT NULL |
|              | currency | VARCHAR(3) | NOT NULL |
| **catalog_items** | id | BIGINT | PK, AUTO_INCREMENT |
|                   | product_id | BIGINT | NOT NULL, FK -> products.id |
|                   | display_name | VARCHAR(255) | NOT NULL |
| **price_policies** | id | BIGINT | PK, AUTO_INCREMENT |
|                    | product_id | BIGINT | NOT NULL, FK -> products.id |
|                    | policy_name | VARCHAR(100) | NOT NULL |
|                    | discount_rate | INT | NOT NULL |
| **coupons** | id | BIGINT | PK, AUTO_INCREMENT |
|            | code | VARCHAR(64) | NOT NULL, UNIQUE |
|            | discount_rate | INT | NOT NULL |
|            | expires_at | TIMESTAMP | nullable |

- `catalog_items`, `price_policies` 테이블은 DB에 존재하지만 JPA 엔티티가 미구현
- `coupons` 테이블은 DB에 존재하지만 CouponService가 영속화를 수행하지 않음

---

## 6. 모듈 3: meditlink-commerce-integration-layer (대외 연동)

### 6.1 패키지 구조

```
com.meditlink.poc.commerce.integration/
├── IntegrationLayerApplication.java        # Spring Boot 엔트리포인트
├── admin/web/
│   └── AdminProductController.java         # 관리자 API
├── bff/web/
│   └── ProductBffController.java           # BFF API (주 진입점)
├── config/
│   └── CoreGrpcClientConfig.java           # gRPC 클라이언트 설정
├── gateway/grpc/
│   └── CoreProductGrpcGateway.java         # Core Service gRPC 호출 어댑터
├── orchestration/
│   ├── ProductOrchestrationService.java    # 요청 오케스트레이션
│   └── dto/                                # HTTP DTO 7개
│       ├── CreateProductHttpRequest.java
│       ├── IssueCouponHttpRequest.java
│       ├── IssueCouponHttpResponse.java
│       ├── PriceQuoteHttpResponse.java
│       ├── ProductGroupHttpResponse.java
│       ├── ProductHttpResponse.java
│       └── ProductPlanHttpResponse.java
├── state/
│   ├── IntegrationRequestLogJpaEntity.java # 요청 로그 엔티티
│   ├── IntegrationRequestLogRepository.java
│   └── RequestLogService.java              # 로그 저장 서비스
└── webhook/web/
    └── ProductWebhookController.java       # 웹훅 수신 엔드포인트
```

### 6.2 레이어 흐름

```
[Client]
  -> ProductBffController (REST /api/bff/*)
    -> ProductOrchestrationService (비즈니스 흐름 조합 + 로그)
      -> CoreProductGrpcGateway (HTTP DTO -> protobuf 변환 + gRPC 호출)
        -> [Core Service gRPC Server]
```

### 6.3 BFF Controller API 목록

| HTTP Method | 경로 | 설명 | 응답 |
|-------------|------|------|------|
| POST | `/api/bff/products` | 상품 생성 | 201 + `{productId}` |
| GET | `/api/bff/products/{productId}` | 상품 조회 | 200 / 404 |
| GET | `/api/bff/products/{productId}/price?couponCode=` | 가격 계산 | 200 / 404 |
| POST | `/api/bff/coupons` | 쿠폰 발급 | 201 |
| GET | `/api/bff/product-groups` | 상품 그룹 목록 | 200 |
| GET | `/api/bff/products/{productId}/plans` | 상품 플랜 목록 | 200 |

### 6.4 Admin Controller API

| HTTP Method | 경로 | 설명 |
|-------------|------|------|
| GET | `/api/admin/products/{productId}` | 관리자 상품 조회 |

### 6.5 Webhook Controller API

| HTTP Method | 경로 | 설명 |
|-------------|------|------|
| POST | `/api/webhooks/products/events` | 상품 이벤트 수신 |

### 6.6 gRPC Gateway 상세

```java
@Component
public class CoreProductGrpcGateway {
    // 3개의 Blocking Stub 주입
    private final ProductServiceGrpc.ProductServiceBlockingStub productStub;
    private final PriceServiceGrpc.PriceServiceBlockingStub priceStub;
    private final CouponServiceGrpc.CouponServiceBlockingStub couponStub;

    // createProduct: CreateProductHttpRequest -> CreateProductRequest(proto) -> productId(String)
    // getProduct: productId -> GetProductRequest(proto) -> ProductHttpResponse(DTO)
    // calculatePrice: productId, couponCode -> CalculatePriceRequest(proto) -> PriceQuoteHttpResponse
    // issueCoupon: IssueCouponHttpRequest -> IssueCouponRequest(proto) -> IssueCouponHttpResponse
    //   - expiresAt null이면 epoch 0으로 전달
}
```

### 6.7 Orchestration Service 상세

```java
@Service
public class ProductOrchestrationService {
    // 모든 메서드에서:
    // 1. CoreProductGrpcGateway 호출
    // 2. RequestLogService.log() 호출 (요청 타입, 참조 ID, 상태, 페이로드)

    // 로그 타입: CREATE_PRODUCT, GET_PRODUCT, CALCULATE_PRICE, ISSUE_COUPON
}
```

### 6.8 gRPC Client 설정

```java
@Configuration
public class CoreGrpcClientConfig {
    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel coreServiceManagedChannel(
        @Value("${core.grpc.host:localhost}") String host,
        @Value("${core.grpc.port:9090}") int port
    ) {
        return ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }
    // + 3개 Blocking Stub Bean 등록
}
```

### 6.9 요청 로그 영속화

```java
@Entity @Table(name = "integration_request_logs")
public class IntegrationRequestLogJpaEntity {
    Long id;                    // PK, AUTO_INCREMENT
    String requestType;         // VARCHAR(50), NOT NULL
    String referenceId;         // VARCHAR(100), nullable
    String status;              // VARCHAR(30), NOT NULL
    @Lob String payloadJson;    // TEXT, nullable
    Instant createdAt;          // TIMESTAMP, NOT NULL
}
```

### 6.10 데이터베이스 스키마 (Integration)

**DB명**: `meditlink_integration` (PostgreSQL, 포트 5432)

#### Liquibase Changelog: `0001-integration-request-logs.yaml`

| 테이블 | 컬럼 | 타입 |
|--------|------|------|
| **integration_request_logs** | id | BIGSERIAL PK |
|                              | request_type | VARCHAR(50) NOT NULL |
|                              | reference_id | VARCHAR(100) |
|                              | status | VARCHAR(30) NOT NULL |
|                              | payload_json | TEXT |
|                              | created_at | TIMESTAMPTZ NOT NULL DEFAULT NOW() |

- 인덱스: `idx_req_logs_type_created (request_type, created_at)`

---

## 7. 런타임 설정 비교

| 항목 | Core Service | Integration Layer |
|------|-------------|-------------------|
| HTTP 포트 | 8081 | 8080 |
| gRPC 포트 | 9090 (서버) | 9090 (클라이언트 대상) |
| DB 이름 | meditlink_commerce | meditlink_integration |
| DB 인증 | postgres / postgres | postgres / postgres |
| Hibernate DDL | validate | validate |
| open-in-view | false | false |
| Actuator | health, info | health, info |
| Liquibase | 활성 | 활성 |

---

## 8. 모듈 간 의존성 및 통신 흐름

### 8.1 Core 내부 모듈 간 의존

```
Catalog --depends on--> Product (via LoadProductPort, @NamedInterface "port-out")
Price   --depends on--> Product (via LoadProductPort, @NamedInterface "port-out")
Coupon  --독립-- (다른 모듈에 의존하지 않음)
Product --독립-- (다른 모듈에 의존하지 않음, 의존 받기만 함)
```

### 8.2 서비스 간 통신

```
Integration Layer ──gRPC (plaintext)──> Core Service
  - ProductService (CreateProduct, GetProduct)
  - PriceService (CalculatePrice)
  - CouponService (IssueCoupon)
```

### 8.3 상세 요청 시퀀스 (상품 생성)

```
1. Client -> POST /api/bff/products (JSON)
2. ProductBffController -> ProductOrchestrationService.createProduct()
3. ProductOrchestrationService -> CoreProductGrpcGateway.createProduct()
4. CoreProductGrpcGateway -> productServiceBlockingStub.createProduct() [gRPC]
5. Core: ProductGrpcEndpoint.createProduct() -> CreateProductUseCase.create()
6. ProductService -> Product.newProduct() -> SaveProductPort.save()
7. ProductPersistenceAdapter -> ProductJpaRepository.save() [DB INSERT]
8. 응답 역순 전파: Product -> proto -> DTO -> JSON
9. ProductOrchestrationService -> RequestLogService.log("CREATE_PRODUCT", ...)
10. Client <- 201 Created { productId: "..." }
```

---

## 9. 테스트 현황

### 현재 존재하는 테스트
| 파일 | 유형 | 내용 |
|------|------|------|
| `ModulithStructureTest.java` | 아키텍처 테스트 | `ApplicationModules.of(CoreServiceApplication.class).verify()` |

### 테스트 커버리지 현황
- 단위 테스트: **0개** (서비스, 도메인, 어댑터)
- 통합 테스트: **0개** (Spring Context, DB, gRPC)
- 계약 테스트: **0개** (proto 호환성)
- E2E 테스트: **0개**

---

## 10. 정량 분석

### 10.1 소스 파일 수 (src/main/java 기준)

| 모듈 | 파일 수 |
|------|---------|
| Core Service | 약 42개 (4 도메인 모듈 + config + application) |
| Integration Layer | 약 17개 |
| **합계** | **약 59개** |

### 10.2 컴포넌트 수

| 유형 | 개수 | 목록 |
|------|------|------|
| `@RestController` | 6개 | ProductCommandController, CatalogQueryController, PriceQueryController, CouponCommandController, ProductBffController, AdminProductController, ProductWebhookController |
| gRPC Endpoint | 3개 | ProductGrpcEndpoint, PriceGrpcEndpoint, CouponGrpcEndpoint |
| UseCase 인터페이스 | 5개 | CreateProduct, GetProduct, ListCatalog, CalculatePrice, IssueCoupon |
| `@Service` | 6개 | ProductService, CatalogService, PriceService, CouponService, ProductOrchestrationService, RequestLogService |
| `@Entity` | 5개 | ProductJpaEntity, ProductGroupJpaEntity, ProductPlanJpaEntity, IntegrationRequestLogJpaEntity + (catalog/price DB 테이블은 엔티티 미구현) |
| `@Component` | 5개 | 3x PersistenceAdapter, CoreProductGrpcGateway, GrpcServerLifecycle |
| `@Configuration` | 1개 | CoreGrpcClientConfig |
| JPA Repository | 4개 | Product, ProductGroup, ProductPlan, IntegrationRequestLog |
| Domain Record | 4개 | Product, CatalogItem, PriceQuote, Coupon |
| DTO Record | 7개 | (Integration Layer DTOs) |

---

## 11. 설계 패턴 분석

### 11.1 적용된 패턴

| 패턴 | 적용 위치 | 설명 |
|------|-----------|------|
| **Hexagonal Architecture** | Core Service 전체 | Port/Adapter 분리로 도메인 보호 |
| **Spring Modulith** | Core Service 4개 모듈 | 모듈 경계 + Named Interface로 접근 제어 |
| **CQRS (부분적)** | Controller 네이밍 | Command/Query 분리 (Command vs Query Controller) |
| **Factory Method** | `Product.newProduct()` | 도메인 객체 생성 캡슐화 |
| **Adapter Pattern** | Gateway, PersistenceAdapter | 기술 구현 세부사항 추상화 |
| **DTO Pattern** | Integration DTOs | 계층 간 데이터 전달 객체 분리 |
| **Orchestration** | ProductOrchestrationService | 다수 서비스 호출 조합 |
| **Audit Trail** | RequestLogService | 모든 요청 로그 DB 저장 |
| **Smart Lifecycle** | GrpcServerLifecycle | Spring 생명주기와 gRPC 서버 연동 |

### 11.2 코드 컨벤션

| 항목 | 규칙 |
|------|------|
| 도메인 모델 | Java `record` 사용 (불변) |
| UseCase | `*UseCase` 인터페이스 |
| Command | `*Command` record |
| 아웃바운드 포트 | `*Port` 인터페이스 |
| 영속 어댑터 | `*PersistenceAdapter` |
| gRPC 어댑터 | `*GrpcEndpoint` |
| REST 어댑터 | `*Controller` |
| HTTP DTO | `*HttpRequest` / `*HttpResponse` record |
| 트랜잭션 | 조회: `@Transactional(readOnly = true)`, 변경: `@Transactional` |
| DI | 생성자 주입 (final 필드) |
| 검증 | `@Valid` + Jakarta Validation 어노테이션 |

---

## 12. 리스크 및 개선 제안

### 12.1 즉시 대응 필요 (Critical)

| # | 항목 | 상세 |
|---|------|------|
| 1 | **테스트 부재** | 단위/통합/계약 테스트 0개. ModulithStructureTest만 존재. 회귀 리스크 매우 높음 |
| 2 | **쿠폰 영속화 미구현** | DB `coupons` 테이블 존재하지만 JPA 엔티티/Repository 미연결. CouponService가 메모리에서만 동작 |
| 3 | **가격 정책 하드코딩** | PriceService에서 "SALE10" 접두어 체크 + 10% 할인이 코드에 직접 작성됨. `price_policies` 테이블 미활용 |

### 12.2 단기 개선 (High)

| # | 항목 | 상세 |
|---|------|------|
| 4 | **ProductGroup/ProductPlan 도메인 모델 부재** | JPA 엔티티만 존재하고 domain record가 없음. 헥사고날 원칙 위반 |
| 5 | **gRPC 에러 처리 불일치** | Product: INVALID_ARGUMENT 에러, Price: found=false, Coupon: 항상 성공. 일관된 에러 정책 필요 |
| 6 | **DB 자격증명 하드코딩** | application.yml에 postgres/postgres 직접 기재. 환경변수/시크릿 관리 필요 |
| 7 | **gRPC plaintext** | CoreGrpcClientConfig에서 `.usePlaintext()` 사용. 운영 환경에서 TLS 필요 |
| 8 | **RequestLogService SQL Injection 가능성** | payloadJson에 `"{\"sku\":\"" + request.sku() + "\"}"` 문자열 직접 조합. JSON 라이브러리 사용 권장 |

### 12.3 중기 개선 (Medium)

| # | 항목 | 상세 |
|---|------|------|
| 9 | **catalog_items 테이블 미활용** | CatalogService가 Product를 직접 조회하여 CatalogItem 생성. catalog_items 테이블 무용 상태 |
| 10 | **javax/jakarta 혼용** | gRPC 생성 코드가 javax.annotation-api에 의존. 향후 호환성 점검 필요 |
| 11 | **관측성 부재** | 분산 추적(Distributed Tracing), 상관관계 ID, 구조화 로깅 미적용 |
| 12 | **GrpcServerLifecycle graceful shutdown** | `server.shutdown()` 사용 중 `server.awaitTermination()` 미호출. 진행 중 요청 유실 가능 |
| 13 | **Integration 오케스트레이션 트랜잭션** | gRPC 호출 성공 후 로그 저장 실패 시 불일치 발생 가능 |

### 12.4 장기 개선 (Low)

| # | 항목 | 상세 |
|---|------|------|
| 14 | **Modulith 이벤트 기반 통신** | 현재 직접 포트 호출 방식. 이벤트 기반 모듈 통신 도입 검토 |
| 15 | **Spring gRPC 공식 스타터** | 현재 수동 GrpcServerLifecycle 구현. Spring gRPC 공식 지원 채택 검토 |
| 16 | **API 버전 관리** | REST API에 버전 경로(/v1/) 미적용 |
| 17 | **Pagination** | 목록 조회 API에 페이징 미적용 (loadAll() 사용) |

---

## 13. 종합 결론

이 프로젝트는 **"계약 분리(common) + 도메인 중심(core) + 대외 연동(integration)"** 이라는 아키텍처 목표를 코드 레벨에서 일관성 있게 구현했다.

**강점:**
- Hexagonal Architecture + Spring Modulith의 교과서적 적용
- gRPC 계약을 common 모듈로 분리하여 서비스 간 결합 최소화
- 도메인 모델에 Java record를 활용한 불변성 확보
- Port/Adapter 분리가 명확하여 기술 교체 용이
- ModulithStructureTest로 아키텍처 경계 자동 검증

**약점:**
- 테스트 커버리지가 극히 낮음 (구조 검증 1개만)
- 일부 DB 테이블과 도메인 코드 간 불일치 (coupons, catalog_items, price_policies)
- 비즈니스 규칙 하드코딩 (가격 할인 정책)
- 운영 환경 준비 미흡 (보안, 관측성, 에러 처리 표준화)

**PoC로서의 완성도:** 아키텍처 패턴 학습 및 기술 조합 검증 목적으로는 충분한 구조적 완성도를 갖추고 있으며, 프로덕션 전환 시 위 개선 사항을 순차적으로 적용해야 한다.
