# PRODUCT-BC.md — Product Bounded Context 상세 설계

## 1. 개요

Product BC는 "우리가 무엇을 파는가"를 정의하는 도메인이다.
카탈로그(상품 묶음), 상품, 가격을 관리하며, Stripe Product/Price와 동기화한다.

### 역할

- 상품 카탈로그 정의 및 관리
- 상품별 기능(Feature) 연결 관리 (Feature 정의 자체는 Feature BC)
- 가격 정의 및 통화별 관리
- 상품 노출 조건, 가격 적용 조건 정의 (평가는 BFF)
- Stripe Product/Price 동기화 (push, 우리 → Stripe)

### 안 하는 것

- 조건 매칭/필터링 (BFF + Shared RuleEngine의 책임)
- 구독/결제 관리 (Billing BC)
- Feature 정의 (Feature BC)
- 할인/쿠폰 (Coupon BC)

---

## 2. Aggregate 구조

3개의 독립 Aggregate. 각각 독립적으로 생성/수정/삭제 가능.

```
Product BC
├── Catalog Aggregate    { Catalog }
├── Product Aggregate    { Product, ProductFeature }
└── Price Aggregate      { Price }

참조 관계 (FK, ID만):
  Product.catalogId → Catalog.catalogId
  Price.productId → Product.productId
  ProductFeature.featureCode → Feature BC (외부)
```

### 분리 근거

| 테스트 | Catalog | Product | Price |
|--------|---------|---------|-------|
| 독립 생성 가능? | ✅ 상품 없이 생성 | ✅ 가격 없이 생성 | ✅ 독립 생성 |
| 독립 수정 가능? | ✅ 이름/조건 변경 | ✅ 기능 추가/제거 | ✅ 조건/금액 변경 |
| 삭제 시 다른 Aggregate 수정 필요? | ❌ (Product는 정책 판단) | ❌ (Price는 정책 판단) | ❌ |
| 다른 Aggregate 없이 의미 있는가? | ✅ 빈 카탈로그 존재 가능 | ✅ 가격 없는 DRAFT 상품 | ✅ (단, productId 참조는 필요) |

---

## 3. Catalog Aggregate

### 3.1 도메인 모델

```java
public class Catalog {
    private CatalogId catalogId;       // UUID
    private String slug;               // nullable, unique, URL용
    private String name;               // not null
    private String description;        // nullable
    private CatalogStatus status;      // DRAFT, ACTIVE, ARCHIVED
    private int displayOrder;
    private Rule condition;            // nullable (null = 조건 없음)
    private Map<String, Object> attributes;
    private Map<String, Object> metadata;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
}
```

### 3.2 상태 전이

```
DRAFT → ACTIVE    (활성화, 하위 상품이 하나 이상 있어야 함? → 정책 결정 필요)
ACTIVE → ARCHIVED (보관, 하위 상품은 어떻게? → 정책 결정 필요)
ARCHIVED → ACTIVE (재활성화 가능)
DRAFT → (삭제)    (DRAFT 상태에서만 삭제 허용? → 정책 결정 필요)
```

> ⚠️ 정책 결정 필요: 상태 전이 시 하위 Product에 대한 처리 정책을 확정해야 함.
> PoC에서는 상태 전이 제약을 느슨하게 가되, TODO 주석으로 남길 것.

### 3.3 불변식 (Invariants)

- `name`은 빈 문자열 불가
- `slug`이 있으면 unique
- `status`는 정의된 값만 허용
- `condition`이 있으면 유효한 Rule 구조여야 함 (RuleValidator)

---

## 4. Product Aggregate

### 4.1 도메인 모델

```java
public class Product {
    private ProductId productId;           // UUID
    private CatalogId catalogId;           // FK, Catalog 참조 (ID만)
    private String externalId;             // Stripe Product ID, unique
    private String name;                   // not null
    private String description;            // nullable
    private String type;                   // "PLAN", "ADDON" (TEXT, 확장 가능)
    private String billingType;            // "RECURRING", "ONE_TIME"
    private ProductStatus status;          // ACTIVE, INACTIVE
    private int displayOrder;
    private Rule condition;                // nullable (null = 모두에게 노출)
    private Map<String, Object> attributes;
    private Map<String, Object> metadata;
    private List<String> tags;
    private List<ProductFeature> features; // 내부 Entity
    private Instant createdAt;
    private Instant updatedAt;

    // ── 도메인 메서드 ──
    public void addFeature(String featureCode, Long quota, Map<String, Object> attrs);
    public void removeFeature(String featureCode);
    public ProductFeature getFeature(String featureCode);
    public List<ProductFeature> getFeatures();
}
```

### 4.2 ProductFeature (내부 Entity)

```java
public class ProductFeature {
    private ProductId productId;           // PK (복합)
    private String featureCode;            // PK (복합), Feature BC 참조
    private Long quota;                    // nullable (BOOLEAN feature는 null)
    private Map<String, Object> attributes;
}
```

**설계 의도:**
- ProductFeature는 Product의 내부 Entity. Product를 통해서만 생성/삭제.
- `featureCode`는 Feature BC의 ID를 참조하지만, FK 제약은 걸지 않음 (BC 간 독립성).
- Application Service에서 Feature BC의 api를 호출해 featureCode 유효성 검증.
- `quota`는 "이 상품이 이 기능을 얼마나 제공하는가". Feature 자체의 속성이 아님.
    - 예: storage Feature, Pro Plan은 50GB, Basic은 10GB → quota가 다름.

### 4.3 Product.type 값

현재 정의된 값. TEXT 타입이므로 DB 변경 없이 확장 가능.

| type | 의미 | 예시 |
|------|------|------|
| `PLAN` | 기본 구독 상품. 단계(tier)가 있을 수 있음 | Basic, Pro, Enterprise |
| `ADDON` | 추가 기능. 다른 상품 구독이 전제될 수 있음 | Analytics Module, AI Export |

### 4.4 Product.billingType 값

| billingType | 의미 | Price 특성 |
|-------------|------|-----------|
| `RECURRING` | 정기 결제 | billingInterval, intervalCount 필수 |
| `ONE_TIME` | 1회 결제 | billingInterval, intervalCount null |

**설계 의도:** billingType은 상품의 과금 성격. 같은 상품이 recurring과 one-time을 동시에 가지지 않음. 다른 과금 방식이 필요하면 별도 상품으로 생성.

### 4.5 불변식

- `name`은 빈 문자열 불가
- `externalId`는 unique, not null (Stripe 동기화 후 설정)
- `type`은 허용된 값만 (Application Service에서 검증)
- `billingType`은 `RECURRING` 또는 `ONE_TIME`
- `catalogId`는 존재하는 Catalog을 참조해야 함 (Application Service에서 검증)
- `features`에서 같은 `featureCode`는 중복 불가

---

## 5. Price Aggregate

### 5.1 도메인 모델

```java
public class Price {
    private PriceId priceId;               // UUID
    private ProductId productId;           // FK, Product 참조 (ID만)
    private String externalId;             // Stripe Price ID, unique
    private String currency;               // "USD", "EUR"
    private long amount;                   // cents 단위
    private String billingInterval;        // "MONTH", "YEAR", nullable (ONE_TIME)
    private Integer intervalCount;         // 1, 3, 6, 12, nullable
    private boolean isDefault;             // 해당 통화의 기본 가격
    private Rule condition;                // nullable (null = 조건 없이 적용)
    private Map<String, Object> attributes;
    private Map<String, Object> metadata;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
}
```

### 5.2 Price 매칭 전략

Price에는 `condition`(Rule DSL)과 `attributes`(key-value) 두 가지가 있다.
매칭 로직은 다음 우선순위로 동작:

```
1. condition이 있으면 → RuleEngine.evaluate(condition, context) 로 평가
2. condition이 없고 attributes에 매칭 가능한 키가 있으면 → attributes 매칭
3. 둘 다 없으면 (condition=null, attributes={}) → 무조건 매칭 (default 후보)
```

**condition 예시 (복잡한 조건):**
```json
{
  "type": "AND",
  "rules": [
    { "field": "active_products", "op": "CONTAINS_ANY", "value": ["pro-plan"] },
    { "field": "subscription_months", "op": "GTE", "value": 6 }
  ]
}
```

**attributes 예시 (단순 매칭):**
```json
{ "audience": "enterprise", "region": "US" }
```
→ context.audience == "enterprise" AND context.region == "US" 이면 매칭

자세한 매칭 로직은 [RULE-ENGINE.md](./RULE-ENGINE.md) 참조.

### 5.3 isDefault 규칙

- 하나의 Product 안에서, 같은 `currency`에 대해 `isDefault=true`인 Price는 **하나만** 존재.
- DB에서 partial unique index로 강제: `UNIQUE(product_id, currency) WHERE is_default = true`
- 매칭되는 조건부 Price가 없을 때 fallback으로 사용.

### 5.4 불변식

- `amount` >= 0 (0 허용: 무료 가격)
- `currency`는 허용된 통화 코드 (현재 USD, EUR)
- `externalId`는 unique, not null (Stripe 동기화 후 설정)
- `productId`는 존재하는 Product을 참조해야 함
- `billingInterval`은 `RECURRING` 상품일 때 필수, `ONE_TIME`일 때 null
- 같은 `(productId, currency)`에서 `isDefault=true`는 하나만
- `condition`이 있으면 유효한 Rule 구조여야 함

### 5.5 Stripe와의 관계

Stripe Price는 금액 수정이 불가능. 따라서:
- 가격 변경 시: 기존 Price 비활성화(status 변경 또는 soft delete) + 새 Price 생성
- Stripe에서도 기존 Price archive + 새 Price 생성으로 동기화

---

## 6. DB 스키마

```sql
CREATE TABLE catalogs (
    catalog_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug          TEXT UNIQUE,
    name          TEXT NOT NULL,
    description   TEXT,
    status        TEXT NOT NULL DEFAULT 'DRAFT',
    display_order INT NOT NULL DEFAULT 0,
    condition     JSONB,
    attributes    JSONB NOT NULL DEFAULT '{}',
    metadata      JSONB NOT NULL DEFAULT '{}',
    tags          TEXT[] NOT NULL DEFAULT '{}',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE products (
    product_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    catalog_id    UUID NOT NULL REFERENCES catalogs(catalog_id),
    external_id   TEXT NOT NULL UNIQUE,
    name          TEXT NOT NULL,
    description   TEXT,
    type          TEXT NOT NULL,
    billing_type  TEXT NOT NULL,
    status        TEXT NOT NULL DEFAULT 'ACTIVE',
    display_order INT NOT NULL DEFAULT 0,
    condition     JSONB,
    attributes    JSONB NOT NULL DEFAULT '{}',
    metadata      JSONB NOT NULL DEFAULT '{}',
    tags          TEXT[] NOT NULL DEFAULT '{}',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE product_features (
    product_id    UUID NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    feature_code  TEXT NOT NULL,
    quota         BIGINT,
    attributes    JSONB NOT NULL DEFAULT '{}',
    PRIMARY KEY (product_id, feature_code)
);

CREATE TABLE prices (
    price_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id       UUID NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    external_id      TEXT NOT NULL UNIQUE,
    currency         TEXT NOT NULL,
    amount           BIGINT NOT NULL,
    billing_interval TEXT,
    interval_count   INT DEFAULT 1,
    is_default       BOOLEAN NOT NULL DEFAULT false,
    condition        JSONB,
    attributes       JSONB NOT NULL DEFAULT '{}',
    metadata         JSONB NOT NULL DEFAULT '{}',
    tags             TEXT[] NOT NULL DEFAULT '{}',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 통화별 default는 하나만
CREATE UNIQUE INDEX idx_prices_default
    ON prices(product_id, currency) WHERE is_default = true;

-- 조회 인덱스
CREATE INDEX idx_products_catalog ON products(catalog_id);
CREATE INDEX idx_products_status ON products(catalog_id, status);
CREATE INDEX idx_product_features_product ON product_features(product_id);
CREATE INDEX idx_product_features_feature ON product_features(feature_code);
CREATE INDEX idx_prices_product ON prices(product_id);

-- Tags GIN
CREATE INDEX idx_catalogs_tags ON catalogs USING GIN (tags);
CREATE INDEX idx_products_tags ON products USING GIN (tags);
CREATE INDEX idx_prices_tags ON prices USING GIN (tags);

-- Attributes GIN
CREATE INDEX idx_products_attributes ON products USING GIN (attributes);
CREATE INDEX idx_prices_attributes ON prices USING GIN (attributes);
```

---

## 7. JSONB 필드 규약

| 필드 | 역할 | 비즈니스 로직에서 참조 | 변경 빈도 |
|------|------|---------------------|----------|
| `condition` | RuleEngine 평가 대상 (노출/매칭 조건) | ✅ RuleEngine이 읽음 | 낮음 |
| `attributes` | 비즈니스 로직 참조 (매칭, 정렬, 분류) | ✅ PriceSelector, 정렬 등 | 중간 |
| `metadata` | 표시/운영용, 로직 참조 안 함 | ❌ 코드에서 안 읽음 | 높음 |
| `tags` | 필터/쿼리/그룹핑 | ✅ 검색 쿼리에서 사용 | 중간 |

### Attribute Definition (코드 상수)

attributes의 키-타입-기본값을 코드에서 정의. 런타임에 검증 및 default 제공.

```java
public enum ProductAttribute {
    TIER("tier", Integer.class, 0),
    EXCLUSIVE_WITH("exclusive_with", List.class, List.of()),
    IDEMPOTENT_BY_OUTPUT("idempotent_by_output", Boolean.class, false);

    private final String key;
    private final Class<?> type;
    private final Object defaultValue;
}

public enum PriceAttribute {
    PRIORITY("priority", Integer.class, 99),
    AUDIENCE("audience", String.class, null),
    REGION("region", String.class, null),
    DISCOUNT_REASON("discount_reason", String.class, null);
}

public enum CatalogAttribute {
    CATALOG_TYPE("catalog_type", String.class, "general"),
    DISPLAY_STYLE("display_style", String.class, "list");
}
```

**사용:**
```java
// 값 읽기 (default 처리 포함)
int tier = AttributeReader.get(product.getAttributes(), ProductAttribute.TIER);
// attributes에 "tier"가 없으면 → 0 (default)

// 값 검증 (저장 시)
AttributeValidator.validate(attributes, ProductAttribute.values());
// 알 수 없는 키 → 경고 로그 (거부하지는 않음, 유연성 유지)
// 타입 불일치 → 에러
```

---

## 8. 예시 데이터

```sql
-- Catalog
INSERT INTO catalogs (catalog_id, slug, name, status, attributes) VALUES
('c1', 'design-suite', 'Design Suite', 'ACTIVE',
 '{"catalog_type": "plan_tier", "display_style": "comparison_table"}');

-- Products
INSERT INTO products (product_id, catalog_id, external_id, name, type, billing_type, status, attributes) VALUES
('p1', 'c1', 'prod_basic', 'Basic Plan', 'PLAN', 'RECURRING', 'ACTIVE',
 '{"tier": 1, "exclusive_with": ["p2", "p3"]}'),
('p2', 'c1', 'prod_pro', 'Pro Plan', 'PLAN', 'RECURRING', 'ACTIVE',
 '{"tier": 2, "exclusive_with": ["p1", "p3"]}'),
('p3', 'c1', 'prod_analytics', 'Analytics Module', 'ADDON', 'RECURRING', 'ACTIVE',
 '{}');

-- Product.condition (Analytics는 Pro 이상만)
UPDATE products SET condition = '{"field": "active_products", "op": "CONTAINS_ANY", "value": ["p2"]}'
WHERE product_id = 'p3';

-- ProductFeatures
INSERT INTO product_features (product_id, feature_code, quota) VALUES
('p1', 'design-editor', null),
('p1', 'storage', 10737418240),
('p2', 'design-editor', null),
('p2', 'storage', 53687091200),
('p2', 'export', 100),
('p3', 'analytics-dashboard', null);

-- Prices
INSERT INTO prices (price_id, product_id, external_id, currency, amount, billing_interval, is_default) VALUES
('pr1', 'p1', 'price_basic_usd', 'USD', 1900, 'MONTH', true),
('pr2', 'p1', 'price_basic_eur', 'EUR', 1700, 'MONTH', true),
('pr3', 'p2', 'price_pro_usd', 'USD', 4900, 'MONTH', true),
('pr4', 'p2', 'price_pro_eur', 'EUR', 4500, 'MONTH', true);

-- 조건부 가격 (Enterprise 고객 할인)
INSERT INTO prices (price_id, product_id, external_id, currency, amount, billing_interval, is_default, condition, attributes) VALUES
('pr5', 'p2', 'price_pro_usd_ent', 'USD', 3900, 'MONTH', false,
 '{"field": "customer_tags", "op": "CONTAINS", "value": "enterprise"}',
 '{"priority": 1, "discount_reason": "enterprise_discount"}');

-- 번들 무료 가격 (Pro 구독자에게 Analytics 무료)
INSERT INTO prices (price_id, product_id, external_id, currency, amount, billing_interval, is_default, condition, attributes) VALUES
('pr6', 'p3', 'price_analytics_usd', 'USD', 1500, 'MONTH', true, null, '{}'),
('pr7', 'p3', 'price_analytics_free', 'USD', 0, 'MONTH', false,
 '{"field": "active_products", "op": "CONTAINS_ANY", "value": ["p2"]}',
 '{"priority": 0, "discount_reason": "pro_bundle_free"}');
```
