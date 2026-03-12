package com.meditlink.poc.commerce.core.coupon.infrastructure.persistence.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "promotion_policies")
public class PromotionPolicyEntity {

    @Id
    @Column(name = "policy_id")
    private UUID policyId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "discount_type", nullable = false)
    private String discountType;

    @Column(name = "discount_value", nullable = false)
    private long discountValue;

    @Type(JsonType.class)
    @Column(name = "eligibility", columnDefinition = "jsonb")
    private Map<String, Object> eligibility;

    @Type(JsonType.class)
    @Column(name = "applicable_product_ids", columnDefinition = "jsonb")
    private List<UUID> applicableProductIds;

    @Column(name = "max_redemptions")
    private Integer maxRedemptions;

    @Column(name = "current_redemptions", nullable = false)
    private int currentRedemptions;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_until")
    private Instant validUntil;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "stripe_coupon_id")
    private String stripeCouponId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PromotionPolicyEntity() {}

    public UUID getPolicyId() { return policyId; }
    public void setPolicyId(UUID policyId) { this.policyId = policyId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public long getDiscountValue() { return discountValue; }
    public void setDiscountValue(long discountValue) { this.discountValue = discountValue; }
    public Map<String, Object> getEligibility() { return eligibility; }
    public void setEligibility(Map<String, Object> eligibility) { this.eligibility = eligibility; }
    public List<UUID> getApplicableProductIds() { return applicableProductIds; }
    public void setApplicableProductIds(List<UUID> applicableProductIds) { this.applicableProductIds = applicableProductIds; }
    public Integer getMaxRedemptions() { return maxRedemptions; }
    public void setMaxRedemptions(Integer maxRedemptions) { this.maxRedemptions = maxRedemptions; }
    public int getCurrentRedemptions() { return currentRedemptions; }
    public void setCurrentRedemptions(int currentRedemptions) { this.currentRedemptions = currentRedemptions; }
    public Instant getValidFrom() { return validFrom; }
    public void setValidFrom(Instant validFrom) { this.validFrom = validFrom; }
    public Instant getValidUntil() { return validUntil; }
    public void setValidUntil(Instant validUntil) { this.validUntil = validUntil; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStripeCouponId() { return stripeCouponId; }
    public void setStripeCouponId(String stripeCouponId) { this.stripeCouponId = stripeCouponId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
