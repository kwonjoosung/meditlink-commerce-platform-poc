# Coupon BC 설계 문서

## 개요

Coupon BC는 프로모션 정책(PromotionPolicy)과 개별 쿠폰(Coupon) 발행/관리를 담당하는 Bounded Context이다.
**"Price = 정가, Coupon = 할인"** 원칙에 따라, 모든 할인/프로모션 로직은 이 BC에서 처리한다.

## 핵심 원칙

- **Price 순수성**: Price는 정가(list price)만 관리. 할인은 Coupon BC가 전담
- **Stripe Coupon 동기화**: PromotionPolicy 생성 시 Stripe Coupon도 생성 (DB = SoT)
- **정책 기반 발행**: 개별 쿠폰은 반드시 PromotionPolicy를 통해 발행
- **Rule Engine 최소화**: eligibility 조건은 JSONB로 저장, 향후 Rule Engine 연동 가능

## Aggregates

### 1. PromotionPolicy (Aggregate Root)

프로모션/할인 정책을 정의. Stripe Coupon과 1:1 매핑.

| 필드 | 타입 | 설명 |
|------|------|------|
| policyId | UUID | PK |
| name | String | 정책 이름 |
| description | String | 설명 |
| discountType | DiscountType | PERCENTAGE, FIXED_AMOUNT |
| discountValue | long | 할인 값 (%, 금액) |
| eligibility | Map (JSONB) | 적용 조건 (세그먼트 등) |
| applicableProductIds | List<UUID> (JSONB) | 적용 가능 상품 목록 (빈 배열 = 전체) |
| maxRedemptions | Integer | 최대 사용 횟수 (null = 무제한) |
| currentRedemptions | int | 현재 사용 횟수 |
| validFrom | Instant | 유효 시작일 |
| validUntil | Instant | 유효 종료일 (null = 무기한) |
| status | PromotionStatus | ACTIVE, INACTIVE |
| stripeCouponId | String | Stripe Coupon ID |

### 2. Coupon (Aggregate Root)

PromotionPolicy에서 발행된 개별 쿠폰.

| 필드 | 타입 | 설명 |
|------|------|------|
| couponId | UUID | PK |
| policyId | UUID | FK → PromotionPolicy |
| code | String | 고유 쿠폰 코드 |
| customerId | String | 발급 대상 고객 |
| discountType | DiscountType | 할인 유형 (정책에서 복사) |
| discountValue | long | 할인 값 (정책에서 복사) |
| status | CouponStatus | ACTIVE, REDEEMED, EXPIRED |
| stripeCouponId | String | Stripe Coupon ID |
| expiresAt | Instant | 만료일시 |
| redeemedAt | Instant | 사용일시 |

## Value Objects / Enums

- **DiscountType**: `PERCENTAGE`, `FIXED_AMOUNT`
- **CouponStatus**: `ACTIVE`, `REDEEMED`, `EXPIRED`
- **PromotionStatus**: `ACTIVE`, `INACTIVE`

## 패키지 구조

```
coupon/
├── domain/
│   ├── Coupon.java
│   ├── CouponStatus.java
│   ├── DiscountType.java
│   ├── PromotionPolicy.java
│   └── PromotionStatus.java
├── application/
│   ├── port/
│   │   ├── CouponRepository.java
│   │   ├── PromotionPolicyRepository.java
│   │   └── StripeCouponSync.java
│   ├── dto/
│   │   ├── CreatePromotionPolicyCommand.java
│   │   └── IssueCouponFromPolicyCommand.java
│   ├── command/
│   │   └── PromotionPolicyService.java
│   └── service/
│       └── CouponService.java
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/
│   │   │   ├── CouponEntity.java
│   │   │   └── PromotionPolicyEntity.java
│   │   ├── repository/
│   │   │   ├── CouponJpaRepository.java
│   │   │   └── PromotionPolicyJpaRepository.java
│   │   └── adapter/
│   │       ├── CouponRepositoryImpl.java
│   │       └── PromotionPolicyRepositoryImpl.java
│   ├── mapper/
│   │   ├── CouponMapper.java
│   │   └── PromotionPolicyMapper.java
│   └── stripe/
│       └── StubStripeCouponSync.java
├── adapter/
│   └── in/
│       ├── web/CouponCommandController.java
│       └── grpc/CouponGrpcEndpoint.java (TODO)
└── api/
    ├── CouponModuleApi.java
    ├── CouponModuleApiImpl.java
    └── dto/ApplicableCouponDto.java
```

## Module API

```java
public interface CouponModuleApi {
    List<ApplicableCouponDto> findApplicablePromotions(UUID productId);
}
```

## 주요 흐름

### 프로모션 정책 생성
1. CreatePromotionPolicyCommand 수신
2. PromotionPolicy 도메인 객체 생성
3. StripeCouponSync.createCoupon() → Stripe Coupon ID 획득
4. policy.assignStripeCouponId() → DB 저장

### 쿠폰 발행 (정책 기반)
1. IssueCouponFromPolicyCommand (policyId, customerId) 수신
2. PromotionPolicy 조회 + isApplicable() 검증
3. Coupon.create() → 정책의 할인 정보 복사
4. policy.incrementRedemptions() → 사용 횟수 증가
5. DB 저장

### 쿠폰 사용
1. couponId로 조회
2. coupon.redeem() → 상태 ACTIVE → REDEEMED, 만료 검증
3. DB 저장

## DB 테이블

```sql
CREATE TABLE promotion_policies (
    policy_id             UUID PRIMARY KEY,
    name                  VARCHAR(200) NOT NULL,
    description           TEXT,
    discount_type         VARCHAR(20) NOT NULL,
    discount_value        BIGINT NOT NULL,
    eligibility           JSONB DEFAULT '{}',
    applicable_product_ids JSONB DEFAULT '[]',
    max_redemptions       INT,
    current_redemptions   INT NOT NULL DEFAULT 0,
    valid_from            TIMESTAMPTZ NOT NULL,
    valid_until           TIMESTAMPTZ,
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    stripe_coupon_id      VARCHAR(100),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE coupons (
    coupon_id        UUID PRIMARY KEY,
    policy_id        UUID NOT NULL REFERENCES promotion_policies(policy_id),
    code             VARCHAR(50) NOT NULL UNIQUE,
    customer_id      VARCHAR(100),
    discount_type    VARCHAR(20) NOT NULL,
    discount_value   BIGINT NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    stripe_coupon_id VARCHAR(100),
    expires_at       TIMESTAMPTZ,
    redeemed_at      TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

## Stripe 연동

- PromotionPolicy 생성 시 → Stripe Coupon 생성 (percent_off 또는 amount_off)
- 개별 Coupon은 정책의 stripeCouponId를 재사용
- DB가 Source of Truth — Stripe는 결제 시점에만 참조
