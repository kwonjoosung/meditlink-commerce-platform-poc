# API-SPEC.md — Product 모듈 API 명세

## 1. 개요

Product 모듈의 Public API는 두 가지 레벨로 제공된다.

1. **모듈 내부 API** (`api/ProductModuleApi.java`) — 다른 BC 모듈이 사용하는 인터페이스
2. **REST API** (`gateway/ProductController.java`) — 프론트엔드/BFF가 사용하는 HTTP 엔드포인트

PoC에서는 둘 다 구현하되, REST API가 모듈 내부 API를 호출하는 구조로 한다.

---

## 2. 모듈 내부 API (ProductModuleApi)

다른 BC 모듈이 Java 인터페이스로 호출. 인메모리 메서드 호출.

```java
public interface ProductModuleApi {

    // ── Query ──

    /**
     * 활성 카탈로그 전체 조회 (하위 Product, ProductFeature, Price 포함).
     * condition, attributes 원본 포함 (매칭은 호출자가 수행).
     */
    List<CatalogDto> getActiveCatalogs();

    /**
     * 특정 상품의 Feature 목록 조회.
     * Entitlement 부여 시 Feature BC에서 호출.
     */
    List<ProductFeatureDto> getProductFeatures(String productId);

    /**
     * 특정 상품 조회 (Price 포함).
     */
    Optional<ProductDto> getProduct(String productId);
}
```

### DTO 정의

```java
public record CatalogDto(
    String catalogId,
    String slug,
    String name,
    String description,
    String status,
    int displayOrder,
    Object condition,               // JSONB → Map or null
    Map<String, Object> attributes,
    Map<String, Object> metadata,
    List<String> tags,
    List<ProductDto> products
) {}

public record ProductDto(
    String productId,
    String catalogId,
    String externalId,
    String name,
    String description,
    String type,
    String billingType,
    String status,
    int displayOrder,
    Object condition,
    Map<String, Object> attributes,
    Map<String, Object> metadata,
    List<String> tags,
    List<ProductFeatureDto> features,
    List<PriceDto> prices
) {}

public record ProductFeatureDto(
    String productId,
    String featureCode,
    Long quota,
    Map<String, Object> attributes
) {}

public record PriceDto(
    String priceId,
    String productId,
    String externalId,
    String currency,
    long amount,
    String billingInterval,
    Integer intervalCount,
    boolean isDefault,
    Object condition,
    Map<String, Object> attributes,
    Map<String, Object> metadata,
    List<String> tags
) {}
```

---

## 3. REST API (ProductController)

### 3.1 Catalog CRUD

```
POST   /api/catalogs                    → 카탈로그 생성
GET    /api/catalogs                    → 카탈로그 목록 조회
GET    /api/catalogs/{catalogId}        → 카탈로그 상세 조회 (하위 상품 포함)
PUT    /api/catalogs/{catalogId}        → 카탈로그 수정
DELETE /api/catalogs/{catalogId}        → 카탈로그 삭제
```

**POST /api/catalogs**

```json
// Request
{
  "name": "Design Suite",
  "slug": "design-suite",
  "description": "디자인 도구 카탈로그",
  "attributes": { "catalog_type": "plan_tier" },
  "metadata": {},
  "tags": ["design"]
}

// Response 201
{
  "catalogId": "uuid",
  "name": "Design Suite",
  "slug": "design-suite",
  "status": "DRAFT",
  ...
}
```

**GET /api/catalogs**

```json
// Query params: ?status=ACTIVE
// Response 200
{
  "catalogs": [
    {
      "catalogId": "uuid",
      "name": "Design Suite",
      "products": [
        {
          "productId": "uuid",
          "name": "Pro Plan",
          "prices": [...],
          "features": [...]
        }
      ]
    }
  ]
}
```

### 3.2 Product CRUD

```
POST   /api/catalogs/{catalogId}/products          → 상품 생성
GET    /api/products/{productId}                    → 상품 상세 조회
PUT    /api/products/{productId}                    → 상품 수정
DELETE /api/products/{productId}                    → 상품 삭제
```

**POST /api/catalogs/{catalogId}/products**

```json
// Request
{
  "name": "Pro Plan",
  "type": "PLAN",
  "billingType": "RECURRING",
  "condition": null,
  "attributes": { "tier": 2, "exclusive_with": ["basic-id"] },
  "metadata": {},
  "tags": ["premium"]
}

// Response 201
// ※ externalId는 Stripe 동기화 후 설정됨
{
  "productId": "uuid",
  "catalogId": "uuid",
  "externalId": "prod_xxx",
  "name": "Pro Plan",
  "type": "PLAN",
  "billingType": "RECURRING",
  "status": "ACTIVE",
  ...
}
```

### 3.3 ProductFeature 관리

```
POST   /api/products/{productId}/features           → Feature 연결
DELETE /api/products/{productId}/features/{code}     → Feature 해제
GET    /api/products/{productId}/features            → Feature 목록
```

**POST /api/products/{productId}/features**

```json
// Request
{
  "featureCode": "storage",
  "quota": 53687091200,
  "attributes": {}
}

// Response 201
{
  "productId": "uuid",
  "featureCode": "storage",
  "quota": 53687091200
}
```

> ⚠️ PoC에서 featureCode 유효성 검증: Feature BC가 아직 없으므로, featureCode를 문자열로 받아 저장만 함. 실제 구현에서는 Feature BC의 api를 호출해 존재 여부를 검증해야 함.

### 3.4 Price CRUD

```
POST   /api/products/{productId}/prices              → 가격 생성
GET    /api/prices/{priceId}                          → 가격 상세
PUT    /api/prices/{priceId}                          → 가격 수정
DELETE /api/prices/{priceId}                          → 가격 비활성화
```

**POST /api/products/{productId}/prices**

```json
// Request
{
  "currency": "USD",
  "amount": 4900,
  "billingInterval": "MONTH",
  "intervalCount": 1,
  "isDefault": true,
  "condition": null,
  "attributes": {},
  "metadata": {},
  "tags": []
}

// Response 201
{
  "priceId": "uuid",
  "productId": "uuid",
  "externalId": "price_xxx",
  "currency": "USD",
  "amount": 4900,
  ...
}
```

**조건부 가격 생성 예시:**

```json
{
  "currency": "USD",
  "amount": 3900,
  "billingInterval": "MONTH",
  "intervalCount": 1,
  "isDefault": false,
  "condition": {
    "field": "customer_tags",
    "op": "CONTAINS",
    "value": "enterprise"
  },
  "attributes": { "priority": 1, "discount_reason": "enterprise_discount" },
  "metadata": {},
  "tags": ["enterprise"]
}
```

---

## 4. 에러 응답

```json
// 400 Bad Request (검증 실패)
{
  "error": "VALIDATION_ERROR",
  "message": "Invalid rule structure",
  "details": ["LeafRule field is required", "Invalid operator: UNKNOWN"]
}

// 404 Not Found
{
  "error": "NOT_FOUND",
  "message": "Product not found: uuid"
}

// 409 Conflict (unique 위반 등)
{
  "error": "CONFLICT",
  "message": "Default price already exists for product uuid with currency USD"
}
```

---

## 5. 가격 결정 흐름 (BFF 참고)

REST API는 원본 데이터를 반환한다. 가격 매칭은 BFF(또는 클라이언트)의 책임이다.

```
BFF 흐름:
1. GET /api/catalogs?status=ACTIVE       → 원본 데이터 전체
2. BFF가 RuleContext 조립 (Billing, Customer 정보 포함)
3. 각 Product의 condition 평가 → 노출 필터링
4. 각 Product의 Price 매칭 → PriceSelector.selectPrice() 
5. 조합된 결과를 프론트에 응답
```

PoC에서 BFF 조합까지 구현할 필요는 없지만, 테스트에서 이 흐름을 검증할 수 있어야 한다.
