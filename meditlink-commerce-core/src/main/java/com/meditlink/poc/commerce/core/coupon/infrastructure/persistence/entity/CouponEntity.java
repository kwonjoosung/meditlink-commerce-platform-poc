package com.meditlink.poc.commerce.core.coupon.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "coupons")
public class CouponEntity {

    @Id
    @Column(name = "coupon_id")
    private UUID couponId;

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "discount_type", nullable = false)
    private String discountType;

    @Column(name = "discount_value", nullable = false)
    private long discountValue;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "stripe_coupon_id")
    private String stripeCouponId;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "redeemed_at")
    private Instant redeemedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CouponEntity() {}

    public UUID getCouponId() { return couponId; }
    public void setCouponId(UUID couponId) { this.couponId = couponId; }
    public UUID getPolicyId() { return policyId; }
    public void setPolicyId(UUID policyId) { this.policyId = policyId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public long getDiscountValue() { return discountValue; }
    public void setDiscountValue(long discountValue) { this.discountValue = discountValue; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStripeCouponId() { return stripeCouponId; }
    public void setStripeCouponId(String stripeCouponId) { this.stripeCouponId = stripeCouponId; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getRedeemedAt() { return redeemedAt; }
    public void setRedeemedAt(Instant redeemedAt) { this.redeemedAt = redeemedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
