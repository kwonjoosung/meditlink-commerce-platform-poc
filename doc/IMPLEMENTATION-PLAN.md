# IMPLEMENTATION-PLAN.md — 구현 계획 (실행용)

> 이 문서는 설계 문서와 기존 코드의 Gap 분석을 기반으로 작성된 실행 계획이다.
> 각 Step 완료 후 상태를 업데이트하며, 피드백 반영 사항도 여기에 기록한다.

---

## 확정된 결정사항

| 항목 | 결정 | 비고 |
|------|------|------|
| 패키지 루트 | `com.meditlink.poc.commerce.core` 유지 | 기존 모듈 구조와 호환 |
| 기존 코드 | 전부 제거 후 재작성 | coupon 모듈 + gRPC 설정 유지 |
| Domain-Entity | 분리 (순수 도메인 + 별도 JPA Entity + Mapper) | ARCHITECTURE.md 기준 |
| JSONB 매핑 | Hypersistence Utils | |
| Liquibase | 기존 changeset 삭제, 새로 시작 | DB 볼륨 초기화 필요 |
| Catalog → ProductGroup | **Catalog Aggregate를 ProductGroup으로 리네이밍** | 피드백 반영 |

---

## 피드백 이력

| 날짜 | 내용 |
|------|------|
| 2026-03-06 | Catalog Aggregate → ProductGroup으로 리네이밍. DB 테이블명, API 경로, 클래스명 모두 변경 |
| 2026-03-06 | Liquibase 기존 changeset 삭제 후 새로 시작 결정 |

---

## 리네이밍 매핑 (Catalog → ProductGroup)

| 설계 문서 원본 | 실제 구현 |
|---------------|----------|
| Catalog | ProductGroup |
| CatalogId | ProductGroupId |
| CatalogStatus | ProductGroupStatus |
| CatalogAttribute | ProductGroupAttribute |
| catalogs (테이블) | product_groups (테이블) |
| catalog_id (컬럼) | product_group_id (컬럼) |
| Product.catalogId | Product.productGroupId |
| /api/catalogs | /api/product-groups |
| CatalogCommandService | ProductGroupCommandService |
| CatalogEntity | ProductGroupEntity |
| CatalogMapper | ProductGroupMapper |
| CatalogDto | ProductGroupDto |
| CatalogRepository | ProductGroupRepository |
| CreateCatalogCommand | CreateProductGroupCommand |

---

## 패키지 구조 (목표)

```
com.meditlink.poc.commerce.core/
├── shared/
│   ├── domain/                    ← 공통 Value Objects
│   │   ├── ProductGroupId.java
│   │   ├── ProductId.java
│   │   └── PriceId.java
│   ├── infra/                     ← 공통 인프라
│   │   └── rule/                  ← Rule Engine
│   │       ├── Rule.java
│   │       ├── CompositeRule.java
│   │       ├── LeafRule.java
│   │       ├── RuleOperator.java
│   │       ├── RuleContext.java
│   │       ├── RuleEngine.java
│   │       ├── RuleOperators.java
│   │       ├── RuleValidator.java
│   │       ├── RuleDeserializer.java
│   │       └── PriceSelector.java
│   └── util/
│
├── product/                       ← Product BC
│   ├── domain/
│   │   ├── productgroup/          ← ProductGroup Aggregate
│   │   │   ├── ProductGroup.java
│   │   │   └── ProductGroupStatus.java
│   │   ├── product/               ← Product Aggregate
│   │   │   ├── Product.java
│   │   │   ├── ProductFeature.java
│   │   │   └── ProductStatus.java
│   │   └── price/                 ← Price Aggregate
│   │       └── Price.java
│   │
│   ├── application/
│   │   ├── command/
│   │   │   ├── ProductGroupCommandService.java
│   │   │   ├── ProductCommandService.java
│   │   │   └── PriceCommandService.java
│   │   ├── query/
│   │   │   └── ProductQueryService.java
│   │   ├── dto/
│   │   │   ├── CreateProductGroupCommand.java
│   │   │   ├── CreateProductCommand.java
│   │   │   ├── CreatePriceCommand.java
│   │   │   └── ...
│   │   └── port/
│   │       ├── ProductGroupRepository.java
│   │       ├── ProductRepository.java
│   │       └── PriceRepository.java
│   │
│   ├── infrastructure/
│   │   ├── persistence/
│   │   │   ├── entity/
│   │   │   │   ├── ProductGroupEntity.java
│   │   │   │   ├── ProductEntity.java
│   │   │   │   ├── ProductFeatureEntity.java
│   │   │   │   └── PriceEntity.java
│   │   │   ├── repository/
│   │   │   │   ├── ProductGroupJpaRepository.java
│   │   │   │   ├── ProductJpaRepository.java
│   │   │   │   ├── ProductFeatureJpaRepository.java
│   │   │   │   └── PriceJpaRepository.java
│   │   │   └── adapter/
│   │   │       ├── ProductGroupRepositoryImpl.java
│   │   │       ├── ProductRepositoryImpl.java
│   │   │       └── PriceRepositoryImpl.java
│   │   ├── stripe/
│   │   │   ├── StripeProductSyncService.java
│   │   │   └── StripePriceSyncService.java
│   │   └── mapper/
│   │       ├── ProductGroupMapper.java
│   │       ├── ProductMapper.java
│   │       └── PriceMapper.java
│   │
│   └── api/
│       ├── ProductModuleApi.java
│       ├── ProductModuleApiImpl.java
│       └── dto/
│           ├── ProductGroupDto.java
│           ├── ProductDto.java
│           ├── ProductFeatureDto.java
│           └── PriceDto.java
│
├── coupon/                        ← 기존 유지 (PoC 범위 외)
├── gateway/                       ← REST Controller
│   ├── ProductController.java
│   └── GlobalExceptionHandler.java
└── config/                        ← 기존 유지
```

---

## Step 1: 프로젝트 구조 전환 + 의존성 정비 ✅

**작업:**
1. `libs.versions.toml`에 stripe-java, hypersistence-utils, archunit 추가
2. `meditlink-commerce-core/build.gradle.kts`에 의존성 추가
3. 기존 코드 제거:
   - `core/catalog/` 전체 삭제
   - `core/price/` 전체 삭제
   - `core/product/` 전체 삭제
   - `core/coupon/` 유지
   - `core/config/` 유지
4. 새 패키지 디렉토리 생성 (빈 package-info.java로 구조만)
5. Liquibase: 기존 changeset (0001, 0002) 삭제, 새 changeset 작성
   - product_groups, products, product_features, prices 테이블
   - JSONB, TEXT[], partial unique index, GIN index
6. docker init SQL 교체 (02-core-final-schema.sql, 04-core-dummy-data.sql)
7. 빌드 확인 + 애플리케이션 기동 확인

**변경 파일:**
- `gradle/libs.versions.toml`
- `meditlink-commerce-core/build.gradle.kts`
- `core/catalog/` 삭제
- `core/price/` 삭제
- `core/product/` 삭제
- `db/changelog/` 새로 작성
- `docker/postgres/init/02-core-final-schema.sql`
- `docker/postgres/init/04-core-dummy-data.sql`

---

## Step 2: Shared 모듈 — Rule Engine + Attribute 시스템 ✅

**작업:**
1. shared/domain/ — ProductGroupId, ProductId, PriceId (UUID wrapper record)
2. shared/infra/rule/ — Rule Engine 전체 (Rule, CompositeRule, LeafRule, RuleOperator, RuleContext, RuleEngine, RuleOperators, RuleValidator, RuleDeserializer, PriceSelector)
3. Attribute 시스템 — ProductGroupAttribute, ProductAttribute, PriceAttribute enum, AttributeReader, AttributeValidator

**테스트:** RuleEngineTest, PriceSelectorTest, RuleValidatorTest, AttributeReaderTest

---

## Step 3: Product BC — Domain 모델 ⬜

**작업:**
1. domain/productgroup/ — ProductGroup, ProductGroupStatus
2. domain/product/ — Product, ProductFeature, ProductStatus
3. domain/price/ — Price

**테스트:** ProductGroupTest, ProductTest, PriceTest

---

## Step 4: Product BC — Persistence ⬜

**작업:**
1. JPA Entity 4개 (JSONB/TEXT[] 매핑)
2. JPA Repository 4개
3. Domain Repository Port 3개
4. Repository Adapter 3개
5. Mapper 3개

**테스트:** @DataJpaTest + Testcontainers

---

## Step 5: Product BC — Application Service ⬜

**작업:**
1. Command DTO 6개
2. ProductGroupCommandService, ProductCommandService, PriceCommandService
3. ProductQueryService

**테스트:** Service 통합 테스트

---

## Step 6: REST API + Module API ⬜

**작업:**
1. ProductController (전체 CRUD)
2. GlobalExceptionHandler
3. ProductModuleApi + Impl
4. 외부 노출 DTO

**테스트:** MockMvc 통합 테스트

---

## Step 7: Stripe 동기화 ⬜

**작업:**
1. Port 인터페이스 2개
2. Stripe 구현체 2개
3. Application Service 연동

**테스트:** Stripe Mock 단위 테스트

---

## Step 8: 통합 테스트 + PoC 검증 ⬜

**작업:**
1. E2E 시나리오 테스트
2. ArchUnit 테스트
3. ModulithStructureTest 업데이트
4. 예시 데이터 시드

---

## 범례

- ⬜ 미착수
- 🔄 진행 중
- ✅ 완료
