# ARCHITECTURE.md — 모듈 구조 및 개발 규칙

## 1. 아키텍처 개요

모듈러 모놀리스 (Modular Monolith). 하나의 Spring Boot 애플리케이션 안에서 Bounded Context별 모듈을 논리적으로 분리한다.

### 핵심 원칙

- **모듈 = Bounded Context**: 하나의 BC가 하나의 모듈(패키지)에 대응
- **인터페이스로 통신**: 모듈 간 참조는 `api/` 패키지의 인터페이스와 DTO를 통해서만
- **이벤트로 연결**: 모듈 간 상태 변경 전파는 도메인 이벤트 (Spring ApplicationEvent)
- **Aggregate 단위 트랜잭션**: 하나의 트랜잭션은 하나의 Aggregate만 변경하는 것이 원칙 (같은 BC 내 실용적 타협 가능)

---

## 2. 패키지 구조

```
com.company.billing/
│
├── shared/                              ← Shared 모듈
│   ├── domain/                          ← 공통 도메인 타입
│   │   ├── CustomerId.java
│   │   ├── Money.java
│   │   └── Currency.java
│   ├── infra/                           ← 공통 인프라
│   │   ├── rule/                        ← Rule Engine
│   │   │   ├── Rule.java                ← sealed interface
│   │   │   ├── CompositeRule.java
│   │   │   ├── LeafRule.java
│   │   │   ├── RuleOperator.java        ← enum
│   │   │   ├── RuleContext.java
│   │   │   ├── RuleEngine.java
│   │   │   ├── RuleValidator.java
│   │   │   └── PriceSelector.java
│   │   └── event/
│   │       └── DomainEvent.java
│   └── util/                            ← 기술 유틸 (JSON 변환 등)
│
├── product/                             ← Product BC (PoC 대상)
│   ├── domain/                          ← 도메인 모델 (순수 Java, 프레임워크 의존 없음)
│   │   ├── catalog/
│   │   │   └── Catalog.java
│   │   ├── product/
│   │   │   ├── Product.java
│   │   │   └── ProductFeature.java
│   │   └── price/
│   │       └── Price.java
│   │
│   ├── application/                     ← Use Case (Application Service)
│   │   ├── command/                     ← 쓰기
│   │   │   ├── CatalogCommandService.java
│   │   │   ├── ProductCommandService.java
│   │   │   └── PriceCommandService.java
│   │   ├── query/                       ← 읽기
│   │   │   └── ProductQueryService.java
│   │   └── dto/                         ← 내부 DTO (Command/Query용)
│   │       ├── CreateCatalogCommand.java
│   │       ├── CreateProductCommand.java
│   │       └── ...
│   │
│   ├── infrastructure/                  ← 인프라 구현
│   │   ├── persistence/                 ← JPA Entity, Repository 구현
│   │   │   ├── entity/
│   │   │   │   ├── CatalogEntity.java
│   │   │   │   ├── ProductEntity.java
│   │   │   │   ├── ProductFeatureEntity.java
│   │   │   │   └── PriceEntity.java
│   │   │   └── repository/
│   │   │       ├── CatalogJpaRepository.java
│   │   │       ├── ProductJpaRepository.java
│   │   │       └── PriceJpaRepository.java
│   │   ├── stripe/                      ← Stripe 동기화
│   │   │   ├── StripeProductSyncService.java
│   │   │   └── StripePriceSyncService.java
│   │   └── mapper/                      ← Domain ↔ Entity 매퍼
│   │       ├── CatalogMapper.java
│   │       ├── ProductMapper.java
│   │       └── PriceMapper.java
│   │
│   └── api/                             ← Public Interface (다른 모듈용)
│       ├── ProductModuleApi.java         ← 인터페이스 정의
│       ├── ProductModuleApiImpl.java     ← 구현
│       └── dto/                          ← 외부 노출 DTO
│           ├── CatalogDto.java
│           ├── ProductDto.java
│           ├── ProductFeatureDto.java
│           └── PriceDto.java
│
├── feature/                             ← Feature BC (PoC 범위 외)
├── billing/                             ← Billing BC (PoC 범위 외)
├── coupon/                              ← Coupon BC (PoC 범위 외)
│
└── gateway/                             ← BFF / Gateway (PoC에서는 Controller 역할)
    └── ProductController.java           ← REST API (BFF 역할)
```

---

## 3. 레이어 규칙

### domain/

- **순수 Java 클래스**. Spring, JPA, 외부 프레임워크 의존 없음.
- 비즈니스 로직, 불변식(invariant), 도메인 이벤트 정의.
- Aggregate Root가 하위 Entity를 관리하는 메서드를 제공.
- 외부 의존 없으므로 단위 테스트가 쉬움.

### application/

- **Use Case 구현**. 트랜잭션 경계 (`@Transactional`).
- domain 객체를 사용하여 비즈니스 흐름을 조율.
- Repository 인터페이스(Port)를 사용. 구현체는 infrastructure에서 주입.
- 다른 BC의 `api/` 인터페이스를 주입받아 사용 가능.

### infrastructure/

- **기술 구현**. JPA Entity, Repository 구현, Stripe SDK 호출.
- domain 모델 ↔ JPA Entity 변환은 mapper에서 수행.
- domain 모델이 JPA 어노테이션을 갖지 않도록 분리.

### api/

- **다른 모듈에 노출하는 인터페이스와 DTO**.
- 이 패키지만 다른 모듈에서 import 가능.
- DTO는 domain 모델과 다를 수 있음 (필요한 정보만 노출).

---

## 4. 의존성 방향

```
gateway → product.api (인터페이스 + DTO만)
gateway → feature.api
gateway → billing.api

product.api ← product.application ← product.domain
                                   ← product.infrastructure

product.application → shared.domain (Money, CustomerId 등)
product.application → shared.infra.rule (RuleEngine 등)
product.infrastructure → product.domain
product.infrastructure → (Spring, JPA, Stripe SDK)
```

### 금지 사항

- `product.domain` → `product.infrastructure` (의존 역전)
- `product.*` → `billing.domain` (다른 BC 내부 참조)
- `product.domain` → Spring/JPA 어노테이션 (프레임워크 의존)

### 강제 방법 (PoC 단계)

ArchUnit 테스트로 검증:

```java
@Test
void domainShouldNotDependOnInfrastructure() {
    noClasses()
        .that().resideInAPackage("..product.domain..")
        .should().dependOnClassesThat()
        .resideInAPackage("..product.infrastructure..")
        .check(classes);
}

@Test
void moduleBoundaries() {
    noClasses()
        .that().resideInAPackage("..product.domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..billing..", "..feature..", "..coupon..")
        .check(classes);
}
```

---

## 5. DB 전략

### 현재 (PoC)

단일 PostgreSQL Database, 단일 Schema. 테이블 이름으로 모듈 구분:

```
catalogs
products
product_features
prices
```

### 향후

필요 시 Schema 분리 (`CREATE SCHEMA product;`) 또는 DB 분리.

### 규칙

- 각 모듈은 자기 테이블만 직접 접근 (Repository를 통해).
- 모듈 간 JOIN은 금지. 필요하면 각 모듈의 api를 통해 데이터 조합.
- 같은 모듈 내 Aggregate 간 읽기 JOIN은 허용 (Query Service에서).

### Domain Model ↔ JPA Entity 분리

domain 패키지의 클래스는 순수 Java. JPA Entity는 infrastructure에 별도로 존재.

```
domain/catalog/Catalog.java          ← 순수 도메인 모델 (JPA 없음)
infrastructure/entity/CatalogEntity.java  ← JPA Entity (@Entity, @Table)
infrastructure/mapper/CatalogMapper.java  ← 변환
```

**이유**: domain 모델이 JPA에 의존하면 DB 변경 시 도메인이 깨짐. 분리하면 domain은 순수 비즈니스 로직만 담당.

**PoC에서의 타협**: 초기에는 domain 모델에 직접 JPA 어노테이션을 붙이는 것도 허용. 다만 이 문서에서는 분리된 구조를 기준으로 기술. 타협 시 반드시 팀과 합의.

---

## 6. 이벤트 전략

### 모듈 내부 (같은 BC)

Application Service에서 직접 호출. 이벤트 불필요.

```java
@Transactional
public Product createProduct(CreateProductCommand cmd) {
    Product product = Product.create(cmd);
    productRepository.save(product);
    stripeProductSync.syncToStripe(product);  // 직접 호출
    return product;
}
```

### 모듈 간 (다른 BC)

Spring ApplicationEvent로 발행. 현재는 인프로세스, 향후 메시지 브로커로 교체 가능.

```java
// Product BC에서 이벤트 발행
applicationEventPublisher.publishEvent(new ProductCreatedEvent(product.getId()));

// Feature BC에서 수신
@EventListener
public void onProductCreated(ProductCreatedEvent event) { ... }
```

### PoC에서는

모듈 간 이벤트는 PoC 범위 외. Product BC 내부의 Stripe 동기화만 구현.

---

## 7. 페어 개발 규칙

> 페어 개발 규칙은 프로젝트 루트의 [CLAUDE.md](../CLAUDE.md) 에 정의되어 있다.
> 계획 먼저, 모르면 물어보기, 작게 나누기, 피드백 루프를 따른다.

---

## 8. 코드 스타일

- 변수/메서드: camelCase
- 클래스: PascalCase
- 패키지: lowercase
- 상수: UPPER_SNAKE_CASE
- DB 컬럼: lower_snake_case
- Optional 사용: domain에서는 허용, DTO에서는 nullable
- null 처리: domain에서 명시적 검증, infrastructure에서 Optional 사용
- 주석: "왜(why)"를 설명하는 주석만. "무엇(what)"은 코드로.
