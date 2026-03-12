# STRIPE-SYNC.md — Stripe 동기화 전략

## 1. 개요

Product BC에서 Stripe으로의 동기화는 **우리 → Stripe (push only)** 방향이다.
상품/가격을 우리 시스템에서 생성하면 Stripe에 동기화하고, externalId로 매핑을 저장한다.

### 동기화 대상

| 우리 엔티티 | Stripe 엔티티 | 시점 |
|-----------|-------------|------|
| Product | Stripe Product | Product 생성/수정 시 |
| Price | Stripe Price | Price 생성 시 |
| ProductFeature | Stripe Product Feature | Feature 연결/해제 시 |

### 동기화하지 않는 것

| 엔티티 | 이유 |
|--------|------|
| Catalog | Stripe에 대응 개념 없음 (우리 내부 그룹핑) |

---

## 2. Product → Stripe Product

### 2.1 생성

```java
// Product 생성 시
public Product createProduct(CreateProductCommand cmd) {
    // 1. 도메인 객체 생성
    Product product = Product.create(cmd);

    // 2. Stripe Product 생성
    String stripeProductId = stripeProductSync.createProduct(product);

    // 3. externalId 설정
    product.setExternalId(stripeProductId);

    // 4. DB 저장
    productRepository.save(product);

    return product;
}
```

### 2.2 Stripe API 호출

```java
public class StripeProductSyncService {

    private final StripeClient stripeClient;

    public String createProduct(Product product) {
        ProductCreateParams params = ProductCreateParams.builder()
            .setName(product.getName())
            .setDescription(product.getDescription())
            .putMetadata("internal_id", product.getProductId().toString())
            .putMetadata("type", product.getType())
            .putMetadata("billing_type", product.getBillingType())
            .build();

        com.stripe.model.Product stripeProduct = com.stripe.model.Product.create(params);
        return stripeProduct.getId();
    }

    public void updateProduct(Product product) {
        com.stripe.model.Product stripeProduct =
            com.stripe.model.Product.retrieve(product.getExternalId());

        ProductUpdateParams params = ProductUpdateParams.builder()
            .setName(product.getName())
            .setDescription(product.getDescription())
            .build();

        stripeProduct.update(params);
    }
}
```

---

## 3. Price → Stripe Price

### 3.1 생성

```java
public String createPrice(Price price, Product product) {
    PriceCreateParams.Builder builder = PriceCreateParams.builder()
        .setProduct(product.getExternalId())
        .setCurrency(price.getCurrency().toLowerCase())
        .setUnitAmount(price.getAmount())
        .putMetadata("internal_id", price.getPriceId().toString());

    // RECURRING인 경우
    if ("RECURRING".equals(product.getBillingType())) {
        builder.setRecurring(
            PriceCreateParams.Recurring.builder()
                .setInterval(mapInterval(price.getBillingInterval()))
                .setIntervalCount(price.getIntervalCount())
                .build()
        );
    }

    com.stripe.model.Price stripePrice = com.stripe.model.Price.create(builder.build());
    return stripePrice.getId();
}
```

### 3.2 가격 수정 (Stripe Price는 amount 변경 불가)

Stripe Price는 금액 수정이 불가능하다. 따라서:

```
가격 변경 흐름:
1. 기존 Price를 비활성화 (우리 DB에서 상태 변경)
2. Stripe에서 기존 Price archive
3. 새 Price 생성 (우리 DB + Stripe)
4. 새 externalId 매핑

이 전체 흐름을 PriceCommandService에서 하나의 트랜잭션으로 관리.
```

```java
public Price updatePriceAmount(PriceId priceId, long newAmount) {
    Price oldPrice = priceRepository.findById(priceId);

    // 1. 기존 Price 비활성화
    oldPrice.deactivate();
    priceRepository.save(oldPrice);
    stripePriceSync.archivePrice(oldPrice.getExternalId());

    // 2. 새 Price 생성
    Price newPrice = Price.createFrom(oldPrice, newAmount);
    String stripeId = stripePriceSync.createPrice(newPrice, getProduct(oldPrice));
    newPrice.setExternalId(stripeId);
    priceRepository.save(newPrice);

    return newPrice;
}
```

---

## 4. ProductFeature → Stripe Product Feature

### 4.1 Feature 연결

```java
public void syncFeatureLink(Product product, String featureCode) {
    // Stripe Product에 Feature 연결
    // ※ Feature BC에서 Feature.externalId (Stripe Feature ID)를 알아야 함
    // PoC에서는 Feature BC가 없으므로 Stripe Feature 동기화는 스킵하거나 mock

    // 실제 구현:
    // String stripeFeatureId = featureModuleApi.getFeature(featureCode).externalId();
    // stripeClient.products().createFeature(product.getExternalId(), stripeFeatureId);
}
```

> ⚠️ PoC 범위: Feature의 Stripe 동기화는 Feature BC가 없으므로, PoC에서는 Product/Price 동기화만 구현. Feature 연결은 우리 DB에만 저장하고 Stripe 동기화는 스킵.

---

## 5. 동기화 실패 처리

### 5.1 기본 전략

```
Product/Price 생성 시:
  1. 도메인 객체 생성 (메모리)
  2. Stripe API 호출
  3. 성공하면 → externalId 설정 + DB 저장
  4. 실패하면 → 예외 발생 + 트랜잭션 롤백 (DB에 저장 안 됨)

결과: 우리 DB에 있으면 Stripe에도 있음이 보장됨.
```

### 5.2 Stripe 호출 실패 시

```java
public Product createProduct(CreateProductCommand cmd) {
    Product product = Product.create(cmd);

    try {
        String stripeId = stripeProductSync.createProduct(product);
        product.setExternalId(stripeId);
    } catch (StripeException e) {
        throw new ExternalSyncException("Failed to sync product to Stripe", e);
        // → 트랜잭션 롤백, DB에 저장 안 됨
    }

    productRepository.save(product);
    return product;
}
```

### 5.3 향후 개선 (PoC 범위 외)

- **재시도 메커니즘**: Stripe 일시 장애 시 exponential backoff 재시도
- **Outbox 패턴**: DB에 먼저 저장하고, 별도 프로세스가 Stripe 동기화 (eventual consistency)
- **불일치 감지 배치**: 주기적으로 우리 DB와 Stripe 상태 비교, 불일치 알림

---

## 6. Stripe 직접 수정 방지

### 6.1 Restricted API Key

Stripe Dashboard에서 Product/Price 직접 수정을 방지하기 위해, restricted API key를 사용.

```
Stripe Dashboard → Developers → API keys → Create restricted key
  - Products: Write (우리 시스템만 수정 가능)
  - Prices: Write
  - 나머지: Read only 또는 None
```

### 6.2 불일치 감지 (향후)

```java
// 배치 job (매일 실행)
@Scheduled(cron = "0 0 3 * * *")
public void detectSyncDrift() {
    List<Product> products = productRepository.findAllActive();
    for (Product product : products) {
        StripeProduct stripeProduct = stripe.products().retrieve(product.getExternalId());
        if (!matches(product, stripeProduct)) {
            alertService.sendDriftAlert(product, stripeProduct);
        }
    }
}
```

---

## 7. externalId 관리

### 7.1 naming convention

| 엔티티 | externalId 형식 | 예시 |
|--------|----------------|------|
| Product | Stripe product ID | `prod_OxFH3tQd5eRrK1` |
| Price | Stripe price ID | `price_1NqJZGClCIKlj` |

### 7.2 externalId 생성 시점

externalId는 Stripe 동기화가 완료된 후에만 설정된다.

```
Product 생성 흐름:
  1. Product 도메인 객체 생성 (externalId = null)
  2. Stripe Product 생성 → stripeId 반환
  3. product.setExternalId(stripeId)
  4. DB 저장 (externalId 포함)

externalId가 null인 Product는 존재하지 않아야 함 (Stripe 동기화 실패 시 롤백).
```

### 7.3 PG 교체 시

externalId 필드명이 `stripe_product_id`가 아닌 `external_id`인 이유:
향후 Stripe가 아닌 다른 PG로 교체할 때, 필드명 변경 없이 다른 ID 체계를 저장할 수 있다.
교체 시 고려사항:
- StripeProductSyncService를 다른 구현체로 교체
- 기존 externalId를 새 PG의 ID로 마이그레이션
- 인터페이스(Port)를 통해 추상화하면 교체 용이

```java
// Port (인터페이스)
public interface PaymentGatewayProductSync {
    String createProduct(Product product);
    void updateProduct(Product product);
}

// Stripe 구현
public class StripeProductSyncService implements PaymentGatewayProductSync { ... }

// 향후 다른 PG 구현
public class OtherPGProductSyncService implements PaymentGatewayProductSync { ... }
```
