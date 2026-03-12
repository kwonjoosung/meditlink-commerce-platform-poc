package com.meditlink.poc.commerce.core.coupon.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Coupon Aggregate Root.
 * PromotionPolicy에서 발행된 개별 쿠폰.
 */
public class Coupon {

    private UUID couponId;
    private UUID policyId;
    private String code;
    private String customerId;
    private DiscountType discountType;
    private long discountValue;
    private CouponStatus status;
    private String stripeCouponId;
    private Instant expiresAt;
    private Instant redeemedAt;
    private Instant createdAt;
    private Instant updatedAt;

    private Coupon() {}

    public static Coupon create(UUID policyId, String code, String customerId,
                                DiscountType discountType, long discountValue, Instant expiresAt) {
        Objects.requireNonNull(code, "code는 null일 수 없습니다");
        var c = new Coupon();
        c.couponId = UUID.randomUUID();
        c.policyId = policyId;
        c.code = code;
        c.customerId = customerId;
        c.discountType = discountType;
        c.discountValue = discountValue;
        c.status = CouponStatus.ACTIVE;
        c.stripeCouponId = null;
        c.expiresAt = expiresAt;
        c.redeemedAt = null;
        c.createdAt = Instant.now();
        c.updatedAt = Instant.now();
        return c;
    }

    public static Coupon reconstitute(UUID couponId, UUID policyId, String code, String customerId,
                                       DiscountType discountType, long discountValue, CouponStatus status,
                                       String stripeCouponId, Instant expiresAt, Instant redeemedAt,
                                       Instant createdAt, Instant updatedAt) {
        var c = new Coupon();
        c.couponId = couponId; c.policyId = policyId; c.code = code; c.customerId = customerId;
        c.discountType = discountType; c.discountValue = discountValue; c.status = status;
        c.stripeCouponId = stripeCouponId; c.expiresAt = expiresAt; c.redeemedAt = redeemedAt;
        c.createdAt = createdAt; c.updatedAt = updatedAt;
        return c;
    }

    public void redeem() {
        if (this.status != CouponStatus.ACTIVE) {
            throw new IllegalStateException("ACTIVE 상태의 쿠폰만 사용 가능합니다");
        }
        if (this.expiresAt != null && Instant.now().isAfter(this.expiresAt)) {
            throw new IllegalStateException("만료된 쿠폰입니다");
        }
        this.status = CouponStatus.REDEEMED;
        this.redeemedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void expire() {
        this.status = CouponStatus.EXPIRED;
        this.updatedAt = Instant.now();
    }

    public void assignStripeCouponId(String stripeCouponId) {
        this.stripeCouponId = stripeCouponId;
        this.updatedAt = Instant.now();
    }

    public UUID getCouponId() { return couponId; }
    public UUID getPolicyId() { return policyId; }
    public String getCode() { return code; }
    public String getCustomerId() { return customerId; }
    public DiscountType getDiscountType() { return discountType; }
    public long getDiscountValue() { return discountValue; }
    public CouponStatus getStatus() { return status; }
    public String getStripeCouponId() { return stripeCouponId; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getRedeemedAt() { return redeemedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
