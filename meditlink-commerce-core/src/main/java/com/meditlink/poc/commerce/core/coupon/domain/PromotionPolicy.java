package com.meditlink.poc.commerce.core.coupon.domain;

import java.time.Instant;
import java.util.*;

/**
 * PromotionPolicy Aggregate Root.
 * 프로모션/할인 정책을 정의. Price를 오염시키지 않고 Stripe Coupon으로 적용.
 */
public class PromotionPolicy {

    private UUID policyId;
    private String name;
    private String description;
    private DiscountType discountType;
    private long discountValue;
    private Map<String, Object> eligibility;
    private List<UUID> applicableProductIds;
    private Integer maxRedemptions;
    private int currentRedemptions;
    private Instant validFrom;
    private Instant validUntil;
    private PromotionStatus status;
    private String stripeCouponId;
    private Instant createdAt;
    private Instant updatedAt;

    private PromotionPolicy() {}

    public static PromotionPolicy create(String name, String description,
                                          DiscountType discountType, long discountValue,
                                          Map<String, Object> eligibility,
                                          List<UUID> applicableProductIds,
                                          Integer maxRedemptions,
                                          Instant validFrom, Instant validUntil) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name은 비어 있을 수 없습니다");
        if (discountValue <= 0) throw new IllegalArgumentException("discountValue는 0보다 커야 합니다");
        if (discountType == DiscountType.PERCENTAGE && discountValue > 100) {
            throw new IllegalArgumentException("PERCENTAGE 할인은 100을 초과할 수 없습니다");
        }
        var p = new PromotionPolicy();
        p.policyId = UUID.randomUUID();
        p.name = name;
        p.description = description;
        p.discountType = discountType;
        p.discountValue = discountValue;
        p.eligibility = eligibility != null ? eligibility : Map.of();
        p.applicableProductIds = applicableProductIds != null ? applicableProductIds : List.of();
        p.maxRedemptions = maxRedemptions;
        p.currentRedemptions = 0;
        p.validFrom = validFrom;
        p.validUntil = validUntil;
        p.status = PromotionStatus.ACTIVE;
        p.stripeCouponId = null;
        p.createdAt = Instant.now();
        p.updatedAt = Instant.now();
        return p;
    }

    public static PromotionPolicy reconstitute(UUID policyId, String name, String description,
                                                DiscountType discountType, long discountValue,
                                                Map<String, Object> eligibility, List<UUID> applicableProductIds,
                                                Integer maxRedemptions, int currentRedemptions,
                                                Instant validFrom, Instant validUntil,
                                                PromotionStatus status, String stripeCouponId,
                                                Instant createdAt, Instant updatedAt) {
        var p = new PromotionPolicy();
        p.policyId = policyId; p.name = name; p.description = description;
        p.discountType = discountType; p.discountValue = discountValue;
        p.eligibility = eligibility != null ? eligibility : Map.of();
        p.applicableProductIds = applicableProductIds != null ? applicableProductIds : List.of();
        p.maxRedemptions = maxRedemptions; p.currentRedemptions = currentRedemptions;
        p.validFrom = validFrom; p.validUntil = validUntil;
        p.status = status; p.stripeCouponId = stripeCouponId;
        p.createdAt = createdAt; p.updatedAt = updatedAt;
        return p;
    }

    public boolean isApplicable(Instant now) {
        if (status != PromotionStatus.ACTIVE) return false;
        if (now.isBefore(validFrom)) return false;
        if (validUntil != null && now.isAfter(validUntil)) return false;
        if (maxRedemptions != null && currentRedemptions >= maxRedemptions) return false;
        return true;
    }

    public boolean isApplicableToProduct(UUID productId) {
        return applicableProductIds.isEmpty() || applicableProductIds.contains(productId);
    }

    public void incrementRedemptions() {
        this.currentRedemptions++;
        this.updatedAt = Instant.now();
    }

    public void deactivate() { this.status = PromotionStatus.INACTIVE; this.updatedAt = Instant.now(); }
    public void assignStripeCouponId(String id) { this.stripeCouponId = id; this.updatedAt = Instant.now(); }

    public UUID getPolicyId() { return policyId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public DiscountType getDiscountType() { return discountType; }
    public long getDiscountValue() { return discountValue; }
    public Map<String, Object> getEligibility() { return eligibility; }
    public List<UUID> getApplicableProductIds() { return applicableProductIds; }
    public Integer getMaxRedemptions() { return maxRedemptions; }
    public int getCurrentRedemptions() { return currentRedemptions; }
    public Instant getValidFrom() { return validFrom; }
    public Instant getValidUntil() { return validUntil; }
    public PromotionStatus getStatus() { return status; }
    public String getStripeCouponId() { return stripeCouponId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
