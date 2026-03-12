# IMPLEMENTATION-GUIDE.md — 구현 가이드

## 1. 구현 순서

아래 순서로 단계별 구현한다. 각 단계를 완료하고 리뷰/피드백 후 다음 단계로 진행.

```
Phase 1: 프로젝트 셋업
Phase 2: Shared 모듈 (Rule Engine)
Phase 3: Product BC — Domain 모델
Phase 4: Product BC — Persistence (JPA Entity + Repository)
Phase 5: Product BC — Application Service (Command + Query)
Phase 6: Product BC — REST API (Controller)
Phase 7: Stripe 동기화
Phase 8: 통합 테스트 + PoC 검증
```

---

## 2. Phase별 상세

### Phase 1: 프로젝트 셋업

```
목표: 프로젝트 구조, 의존성, DB 설정

작업:
□ Spring Boot 프로젝트 생성 (Java 17+, Spring Boot 3.x)
□ 의존성 추가
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - postgresql
  - stripe-java
  - archunit (테스트)
  - lombok (선택)
□ 패키지 구조 생성 (ARCHITECTURE.md 기준)
□ PostgreSQL 연결 설정
□ DB 마이그레이션 (Flyway 또는 직접 SQL)
  - PRODUCT-BC.md의 스키마 적용

확인:
□ 애플리케이션 기동 확인
□ DB 테이블 생성 확인
```

### Phase 2: Shared 모듈 — Rule Engine

```
목표: Rule 구조, 평가 로직, PriceSelector 구현

작업:
□ Rule sealed interface (CompositeRule, LeafRule)
□ RuleOperator enum
□ RuleContext
□ RuleEngine.evaluate()
□ RuleOperators (연산자별 구현)
□ RuleValidator
□ PriceSelector.selectPrice()
□ RuleDeserializer (JSONB → Rule 변환)
□ AttributeReader (default 처리)
□ Attribute Definition enum (ProductAttribute, PriceAttribute, CatalogAttribute)

테스트:
□ RuleEngine 단위 테스트
  - null rule → true
  - 단순 LeafRule (각 연산자)
  - CompositeRule (AND, OR, NOT)
  - 중첩 조건
  - context에 필드 없을 때
□ PriceSelector 단위 테스트
  - default price만 있을 때
  - condition 매칭
  - attributes 매칭
  - priority 순서
  - 통화 필터링
  - 매칭 없을 때 fallback
□ RuleValidator 단위 테스트
  - 유효한 룰
  - 빈 rules
  - NOT에 2개 이상 rules
  - 깊이 초과
  - 잘못된 연산자
□ AttributeReader 테스트
  - 명시적 값 읽기
  - default 값 읽기
  - null attributes

확인:
□ 전체 테스트 통과
□ 코드 리뷰
```

### Phase 3: Product BC — Domain 모델

```
목표: 순수 Java 도메인 클래스 구현 (프레임워크 의존 없음)

작업:
□ Value Objects
  - CatalogId, ProductId, PriceId
  - CatalogStatus (DRAFT, ACTIVE, ARCHIVED)
  - ProductStatus (ACTIVE, INACTIVE)
□ Catalog 도메인 클래스
  - 생성, 수정, 상태 전이 메서드
  - 불변식 검증
□ Product 도메인 클래스
  - 생성, 수정 메서드
  - addFeature, removeFeature, getFeatures
  - 불변식 검증
□ ProductFeature 도메인 클래스
□ Price 도메인 클래스
  - 생성 메서드
  - 불변식 검증 (amount >= 0, currency 유효 등)

테스트:
□ Catalog 단위 테스트
  - 생성 시 기본 상태 = DRAFT
  - 이름 빈 문자열 거부
  - 상태 전이
□ Product 단위 테스트
  - Feature 추가/제거
  - 같은 featureCode 중복 추가 거부
  - billingType 유효성
□ Price 단위 테스트
  - amount < 0 거부
  - condition 유효성 검증 (RuleValidator 연동)

확인:
□ 도메인 클래스에 Spring/JPA 어노테이션 없음
□ 전체 테스트 통과
```

### Phase 4: Product BC — Persistence

```
목표: JPA Entity, Repository, Domain ↔ Entity 매핑

작업:
□ JPA Entity 클래스
  - CatalogEntity (@Entity, @Table("catalogs"))
  - ProductEntity (@Entity, @Table("products"))
  - ProductFeatureEntity (@Entity, @Table("product_features"))
  - PriceEntity (@Entity, @Table("prices"))
  - JSONB 매핑 (Hibernate Types 또는 AttributeConverter)
  - TEXT[] 매핑
□ JPA Repository 인터페이스
  - CatalogJpaRepository (JpaRepository 상속)
  - ProductJpaRepository
  - PriceJpaRepository
  - ProductFeatureJpaRepository
□ Domain ↔ Entity Mapper
  - CatalogMapper (Catalog ↔ CatalogEntity)
  - ProductMapper
  - PriceMapper
□ Repository 구현 (Port → Adapter)
  - CatalogRepository (도메인 Port 인터페이스)
  - CatalogRepositoryImpl (JPA 사용 구현체)

테스트:
□ Repository 통합 테스트 (@DataJpaTest)
  - Catalog CRUD
  - Product CRUD (with ProductFeature)
  - Price CRUD
  - JSONB 필드 저장/조회
  - tags 배열 저장/조회
  - isDefault unique 제약 확인
  - ON DELETE CASCADE 확인

확인:
□ DB 왕복 테스트 (저장 → 조회 → 값 일치)
□ JSONB 역직렬화 정상
```

### Phase 5: Product BC — Application Service

```
목표: CRUD + 비즈니스 로직 구현

작업:
□ Command DTOs
  - CreateCatalogCommand, UpdateCatalogCommand
  - CreateProductCommand, UpdateProductCommand
  - CreatePriceCommand
  - AddProductFeatureCommand
□ CatalogCommandService
  - createCatalog, updateCatalog, deleteCatalog
  - 상태 전이 로직
□ ProductCommandService
  - createProduct, updateProduct, deleteProduct
  - addFeature, removeFeature
  - catalogId 존재 확인
□ PriceCommandService
  - createPrice
  - updatePrice (비활성화 + 새로 생성)
  - isDefault 중복 검증
□ ProductQueryService
  - getActiveCatalogs (전체 JOIN 쿼리)
  - getProduct (단건 상세)
  - getProductFeatures (Feature 목록)

테스트:
□ Service 통합 테스트
  - 정상 CRUD 흐름
  - 존재하지 않는 catalogId로 Product 생성 시 에러
  - isDefault 중복 시 에러
  - condition 유효성 검증
  - 상태 전이 규칙

확인:
□ 전체 테스트 통과
□ 트랜잭션 경계 확인
```

### Phase 6: Product BC — REST API

```
목표: HTTP 엔드포인트 구현

작업:
□ ProductController
  - Catalog CRUD endpoints
  - Product CRUD endpoints
  - ProductFeature endpoints
  - Price CRUD endpoints
□ 에러 핸들링
  - GlobalExceptionHandler
  - 검증 실패 → 400
  - Not found → 404
  - Unique 위반 → 409
□ ProductModuleApi 인터페이스 + 구현
  - 다른 모듈용 내부 API

테스트:
□ Controller 통합 테스트 (@SpringBootTest + MockMvc)
  - 전체 CRUD 엔드포인트
  - 에러 응답 형식
  - JSONB 필드 직렬화/역직렬화
□ API 스모크 테스트
  - 실제 HTTP 요청으로 전체 흐름 확인

확인:
□ API-SPEC.md와 일치하는 요청/응답 구조
```

### Phase 7: Stripe 동기화

```
목표: Product/Price 생성 시 Stripe 동기화

작업:
□ PaymentGatewayProductSync 인터페이스 (Port)
□ PaymentGatewayPriceSync 인터페이스 (Port)
□ StripeProductSyncService 구현
  - createProduct, updateProduct
□ StripePriceSyncService 구현
  - createPrice, archivePrice
□ Application Service에 Stripe 동기화 연동
  - Product 생성 → Stripe sync → externalId 설정
  - Price 생성 → Stripe sync → externalId 설정
□ Stripe 실패 시 롤백 처리

테스트:
□ Stripe Mock을 사용한 단위 테스트
  - 동기화 성공 시 externalId 설정 확인
  - 동기화 실패 시 예외 발생 + DB 저장 안 됨
□ (선택) Stripe Test Mode로 통합 테스트
  - 실제 Stripe API 호출
  - Product/Price 생성 확인

확인:
□ externalId 없는 레코드가 DB에 없음 확인
□ Stripe Dashboard에서 생성된 Product/Price 확인 (Test Mode)
```

### Phase 8: 통합 테스트 + PoC 검증

```
목표: 전체 흐름 검증, PoC 완료 확인

작업:
□ End-to-end 시나리오 테스트
  1. 카탈로그 생성
  2. 상품 생성 (→ Stripe Product 동기화)
  3. Feature 연결
  4. 가격 생성 (default + 조건부) (→ Stripe Price 동기화)
  5. 카탈로그 조회 (전체 데이터 반환)
  6. RuleEngine으로 상품 필터링 시뮬레이션
  7. PriceSelector로 가격 매칭 시뮬레이션
□ ArchUnit 테스트
  - 모듈 경계 검증
  - domain → infrastructure 의존 없음
□ 예시 데이터 시드 스크립트

확인:
□ PRODUCT-BC.md 예시 데이터 기반 시나리오 통과
□ 조건부 가격 매칭 정상 동작
□ Stripe 동기화 정상
```

---

## 3. PoC 완료 기준

```
다음 항목이 모두 충족되면 PoC 완료:

✅ 기능
  □ Catalog CRUD 동작
  □ Product CRUD 동작 (Feature 연결 포함)
  □ Price CRUD 동작 (조건부 가격 포함)
  □ Stripe Product/Price 동기화 동작
  □ RuleEngine 조건 평가 동작
  □ PriceSelector 가격 매칭 동작

✅ 아키텍처
  □ DDD 패키지 구조 준수
  □ domain 레이어에 프레임워크 의존 없음
  □ Aggregate 간 독립성 (별도 Repository, 별도 Command Service)
  □ ArchUnit 테스트 통과

✅ 테스트
  □ RuleEngine 단위 테스트 100% 통과
  □ 도메인 모델 단위 테스트 통과
  □ Repository 통합 테스트 통과
  □ API 통합 테스트 통과
  □ End-to-end 시나리오 통과

✅ 문서
  □ 이 설계 문서와 구현이 일치
  □ 불일치 사항이 있으면 문서 업데이트 완료
```

---

## 4. 의사결정 필요 사항 (구현 전 확정)

> 아래 항목들은 구현 중 결정이 필요한 사항이다.
> 구현 시작 전 또는 해당 Phase에서 페어와 합의해야 한다.

### 아키텍처

| 항목 | 선택지 | 기본 방향 |
|------|--------|----------|
| Domain ↔ JPA Entity 분리 여부 | 분리 vs 통합 | 분리 권장 (PoC에서는 타협 가능) |
| ID 타입 | UUID vs String wrapper | UUID 기반 wrapper class |
| JSONB 매핑 방식 | Hibernate Types vs AttributeConverter | 페어와 합의 |
| tags 타입 | TEXT[] vs JSONB array | TEXT[] (GIN 인덱스 용이) |

### 비즈니스 정책

| 항목 | 선택지 | 기본 방향 |
|------|--------|----------|
| Catalog 삭제 시 하위 Product | 함께 삭제 vs 거부 vs 이동 | 페어와 합의 |
| Product 삭제 시 하위 Price | CASCADE vs 거부 | CASCADE (DB 레벨) |
| DRAFT Catalog에만 삭제 허용? | 예 vs 아니오 | 페어와 합의 |
| Catalog 활성화 시 최소 Product 수 | 있음 vs 없음 | 없음 (PoC) |
| externalId 생성 전략 | Stripe 먼저 vs DB 먼저 | Stripe 먼저 (5.2 참조) |

### Rule Engine

| 항목 | 선택지 | 기본 방향 |
|------|--------|----------|
| nonMatchingKeys 관리 | 코드 상수 vs AttributeDefinition | 코드 상수 (PoC) |
| 알 수 없는 attribute 키 | 경고 vs 거부 | 경고 로그 (유연성) |
| Rule 최대 깊이 | 3 vs 5 vs 10 | 5 |

---

## 5. 테스트 전략

### 단위 테스트 (Unit)

- 대상: Domain 모델, RuleEngine, RuleValidator, PriceSelector
- 프레임워크: JUnit 5
- 외부 의존 없음 (DB, Stripe 없이 동작)
- 빠름 (< 1초)

### 통합 테스트 (Integration)

- 대상: Repository, Application Service
- 프레임워크: JUnit 5 + @SpringBootTest + @DataJpaTest
- Testcontainers로 PostgreSQL 실행 (또는 H2, 단 JSONB 제약 있음)
- Stripe는 Mock 또는 Test Mode

### API 테스트 (E2E)

- 대상: REST Controller
- 프레임워크: MockMvc 또는 TestRestTemplate
- 전체 Spring Context 기동
- DB + (Mock) Stripe 포함

### 테스트 네이밍

```java
@Test
void createProduct_withValidCommand_shouldCreateAndSyncToStripe() { ... }

@Test
void evaluate_withAndCondition_shouldReturnTrueWhenAllMatch() { ... }

@Test
void selectPrice_withNoMatchingCondition_shouldReturnDefaultPrice() { ... }
```

패턴: `{메서드}_{상황}_{기대결과}`
