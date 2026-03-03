# meditlink-commerce-platform-poc 상세 분석

## 1) 분석 범위 및 방법
- 본 문서는 저장소 전체 구조, 아키텍처 패턴, 모듈 책임, 런타임 흐름, 데이터/계약, 품질 상태를 정리한 기술 분석 문서다.
- 분석 근거는 코드베이스 직접 스캔(`glob/read/grep`) + 병렬 explore/librarian 결과를 합성했다.
- 분석 시점 기준 워크스페이스 루트: `commerce_test`.

## 2) 저장소 개요
- 프로젝트 성격: 학습용 PoC (Gradle Multi Module + Spring Modulith + Hexagonal + gRPC).
- 루트 모듈 구성 (`settings.gradle.kts`):
  - `meditlink-commerce-common`
  - `meditlink-commerce-core-service`
  - `meditlink-commerce-integration-layer`
- 주요 엔트리포인트:
  - Core: `meditlink-commerce-core-service/src/main/java/com/meditlink/poc/commerce/core/CoreServiceApplication.java`
  - Integration: `meditlink-commerce-integration-layer/src/main/java/com/meditlink/poc/commerce/integration/IntegrationLayerApplication.java`

## 3) 멀티 모듈 아키텍처 분석

### 3.1 `meditlink-commerce-common` (계약 공유 계층)
- 역할: gRPC `proto` 계약과 generated stub 공유.
- 핵심 근거:
  - `meditlink-commerce-common/src/main/proto/commerce/v1/product_service.proto`
  - `meditlink-commerce-common/src/main/proto/commerce/v1/price_service.proto`
  - `meditlink-commerce-common/src/main/proto/commerce/v1/coupon_service.proto`
  - `meditlink-commerce-common/build.gradle.kts`
- 특징:
  - `protobuf` 플러그인으로 Java/grpc 소스 생성 후 `sourceSets`에 포함.
  - Core/Integration 모두 동일 계약 타입에 의존하도록 결합점 단일화.

### 3.2 `meditlink-commerce-core-service` (핵심 도메인 계층)
- 역할: 실제 비즈니스 유스케이스/도메인 규칙 실행.
- 특징:
  - Spring Modulith + Hexagonal 구조를 도메인 단위(`product`, `price`, `coupon`, `catalog`)로 운영.
  - HTTP/REST + gRPC 서버 엔드포인트를 동시에 보유.
  - Liquibase 기반 스키마 이력 관리.
- 대표 파일:
  - `meditlink-commerce-core-service/src/main/java/com/meditlink/poc/commerce/core/product/application/service/ProductService.java`
  - `meditlink-commerce-core-service/src/main/java/com/meditlink/poc/commerce/core/product/adapter/in/grpc/ProductGrpcEndpoint.java`
  - `meditlink-commerce-core-service/src/main/java/com/meditlink/poc/commerce/core/config/GrpcServerLifecycle.java`
  - `meditlink-commerce-core-service/src/main/resources/db/changelog/db.changelog-master.yaml`

### 3.3 `meditlink-commerce-integration-layer` (대외 연동 계층)
- 역할: 외부 채널(BFF/Admin/Webhook) 요청 처리, 오케스트레이션, core gRPC 호출, 요청 로그 저장.
- 특징:
  - BFF Controller -> Orchestration Service -> gRPC Gateway -> Core 호출 흐름.
  - 자체 DB(`meditlink_integration`)에 요청 로그 저장.
- 대표 파일:
  - `meditlink-commerce-integration-layer/src/main/java/com/meditlink/poc/commerce/integration/bff/web/ProductBffController.java`
  - `meditlink-commerce-integration-layer/src/main/java/com/meditlink/poc/commerce/integration/orchestration/ProductOrchestrationService.java`
  - `meditlink-commerce-integration-layer/src/main/java/com/meditlink/poc/commerce/integration/gateway/grpc/CoreProductGrpcGateway.java`
  - `meditlink-commerce-integration-layer/src/main/resources/db/changelog/db.changelog-master.yaml`

## 4) 레이어드/헥사고날 패턴 적합성

### 4.1 포트-어댑터 분리
- Core 내부에서 `application.port.in` 인터페이스를 통해 유스케이스 진입점을 명시.
- 서비스 구현은 포트 인터페이스를 구현하고, 구체 영속/전송 기술은 adapter 계층으로 분리.
- 근거:
  - `CreateProductUseCase`, `GetProductUseCase`, `ListProductGroupUseCase` 등 (`.../application/port/in/*`)
  - `ProductService`에서 `SaveProductPort`, `LoadProductPort` 주입 사용.

### 4.2 입력 어댑터 다중화
- 동일 도메인 유스케이스를 Web + gRPC 인바운드 어댑터로 노출.
- 근거:
  - Web: `ProductCommandController`, `ProductGroupPlanQueryController`, `PriceQueryController`, `CouponCommandController`, `CatalogQueryController`
  - gRPC: `ProductGrpcEndpoint`, `PriceGrpcEndpoint`, `CouponGrpcEndpoint`

### 4.3 통합 계층의 오케스트레이션 책임
- Integration은 도메인 규칙을 직접 구현하지 않고, 호출 조합 + 상태 로깅 + 응답 형태 변환에 집중.
- `ProductOrchestrationService`가 요청 유형별 로그(`CREATE_PRODUCT`, `GET_PRODUCT`, `CALCULATE_PRICE` 등)를 남김.

## 5) Modulith 관점 분석
- 모듈 정의 방식:
  - `package-info.java` + `@ApplicationModule` 사용 (예: `.../core/product/package-info.java`).
- 구조 검증 테스트:
  - `meditlink-commerce-core-service/src/test/java/com/meditlink/poc/commerce/core/ModulithStructureTest.java`
  - `ApplicationModules.of(CoreServiceApplication.class).verify()` 호출로 아키텍처 회귀 감지.
- 의의:
  - 컴파일 성공과 별개로 모듈 경계 침범을 테스트 단계에서 차단.

## 6) 런타임 데이터/요청 흐름

### 6.1 대표 플로우: 상품 조회
1. 클라이언트가 Integration BFF REST 호출 (`/api/bff/products/{productId}`)
2. `ProductBffController`가 `ProductOrchestrationService`에 위임
3. `CoreProductGrpcGateway`가 Core의 `ProductService` gRPC 호출
4. Core의 `ProductGrpcEndpoint`가 유스케이스(`GetProductUseCase`) 실행
5. 결과를 Integration DTO로 변환 후 응답 + 요청 로그 적재

### 6.2 gRPC 서버 라이프사이클
- Core는 `GrpcServerLifecycle`에서 `BindableService` 목록을 주입받아 서버 시작.
- endpoint 구현체 직접 의존이 아닌 컬렉션 주입 방식으로 모듈 결합을 낮춤.

## 7) 기술 스택 및 버전

### 7.1 빌드/언어
- Gradle Kotlin DSL 멀티모듈.
- Java Toolchain 25 강제 (`build.gradle.kts`).
- 중앙 버전 카탈로그: `gradle/libs.versions.toml`.

### 7.2 프레임워크/라이브러리
- Spring Boot `4.0.0`
- Spring Modulith `2.0.0`
- gRPC `1.76.0`
- Protobuf `4.32.0`
- Liquibase (Spring Boot BOM 관리)
- PostgreSQL `42.7.7`

### 7.3 관찰 포인트
- `javax.annotation-api` 의존성이 남아 있어, Jakarta 전환 전략과 혼용 여부 점검 필요.

## 8) 설정/인프라 분석

### 8.1 애플리케이션 설정
- Core 설정: `meditlink-commerce-core-service/src/main/resources/application.yml`
  - HTTP 8081, gRPC 9090, DB `meditlink_commerce`
- Integration 설정: `meditlink-commerce-integration-layer/src/main/resources/application.yml`
  - HTTP 8080, Core gRPC 대상 host/port, DB `meditlink_integration`

### 8.2 DB 마이그레이션
- Core changelog master에서 도메인 테이블 변경 이력 include:
  - `0001-initial-product-domain.yaml`
  - `0002-product-group-plan.yaml`
- Integration은 요청 로그 테이블 이력 분리 관리.

## 9) 코드 컨벤션/스타일 분석

### 9.1 네이밍 규칙
- UseCase: `*UseCase`
- Command: `*Command`
- Gateway: `*Gateway`
- Controller: `*Controller`
- DTO/응답: `*HttpResponse`, `*HttpRequest`

### 9.2 불변 데이터 모델 지향
- Domain/DTO 대부분 `record` 사용.
- 값 전달 객체의 단순성과 불변성을 확보하는 방향.

### 9.3 트랜잭션 정책
- 조회 중심 서비스는 `@Transactional(readOnly = true)`.
- 변경 흐름에는 `@Transactional` 적용.

### 9.4 입력 검증
- Web 입력에 `jakarta.validation` + `@Valid` 적용.

## 10) 정량 스냅샷 (코드베이스 관찰)
- `src/main/java` 기준 주요 Java 파일: 52개(검색 기준).
- `@RestController`: 8개.
- gRPC `ImplBase` 엔드포인트: 3개.
- `UseCase` 인터페이스: 7개.
- `@Entity`: 1개 (integration 요청 로그 엔티티).
- 테스트 파일: 현재 확인 기준 1개 (`ModulithStructureTest`).

## 11) 품질/테스트 상태 해석
- 강점:
  - 아키텍처 경계 검증 테스트가 존재.
  - 멀티 모듈/계약 분리/레이어 분리가 명확.
- 보완 필요:
  - 도메인/어댑터 단위 테스트, 통합 테스트, 계약 테스트가 매우 제한적.
  - 현재 테스트 커버리지 관점에서는 회귀 리스크가 높은 편.

## 12) 리스크 및 개선 제안

### 12.1 단기(우선순위 높음)
1. 핵심 유스케이스 단위 테스트 추가 (`product`, `price`, `coupon`).
2. Integration-Orchestration-gRPC Gateway 통합 테스트 추가.
3. gRPC 에러코드/예외 매핑 정책 문서화 및 일관화.

### 12.2 중기
1. Liquibase 변경셋 네이밍/경로 정책을 모듈 단위로 더 명확히 표준화.
2. `javax`/`jakarta` 혼용 여부 정리 및 향후 호환 정책 확정.
3. 운영 관점의 관측성(트레이싱/메트릭/상관관계 ID) 강화.

### 12.3 장기
1. BFF/Orchestration 계층의 기능 확장 시 도메인 규칙 침투 방지 가이드 수립.
2. Modulith 이벤트 기반 모듈 통신(필요 시) 도입 검토.

## 13) 외부 레퍼런스 기반 베스트 프랙티스 체크
- Spring Modulith 공식 가이드: 모듈 테스트/문서화 자동화 활용 가치가 높다.
- gRPC/Spring 생태계: 공식 Spring gRPC 스타터 채택 여부와 현재 구성의 정합성 점검 권장.
- Liquibase 멀티모듈: `logicalFilePath`, context/label 전략 도입 시 환경별 안전성 향상.
- Hexagonal 원칙: 도메인에 프레임워크 의존 최소화 원칙을 현재 구조에서 계속 유지 필요.

참고: 외부 버전/권고는 시점에 따라 변동 가능하므로 적용 전 현재 릴리스 노트 재검증이 필요하다.

## 14) 종합 결론
- 이 프로젝트는 "계약 분리(common) + 도메인 중심(core) + 대외 연동(integration)"이라는 학습 목표를 구조적으로 잘 구현했다.
- 특히 Modulith 경계 검증과 gRPC 계약 중심 분리는 설계 의도가 코드에 일관되게 반영되어 있다.
- 다만 테스트 밀도와 운영 관점 품질(관측성/오류정책 표준화)은 다음 단계에서 반드시 보강해야 한다.
