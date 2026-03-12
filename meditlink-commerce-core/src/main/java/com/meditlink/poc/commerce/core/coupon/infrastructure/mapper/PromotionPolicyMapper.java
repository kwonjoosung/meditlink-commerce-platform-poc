package com.meditlink.poc.commerce.core.coupon.infrastructure.mapper;

import com.meditlink.poc.commerce.core.coupon.domain.DiscountType;
import com.meditlink.poc.commerce.core.coupon.domain.PromotionPolicy;
import com.meditlink.poc.commerce.core.coupon.domain.PromotionStatus;
import com.meditlink.poc.commerce.core.coupon.infrastructure.persistence.entity.PromotionPolicyEntity;

public final class PromotionPolicyMapper {

    private PromotionPolicyMapper() {}

    public static PromotionPolicy toDomain(PromotionPolicyEntity entity) {
        return PromotionPolicy.reconstitute(
                entity.getPolicyId(),
                entity.getName(),
                entity.getDescription(),
                DiscountType.valueOf(entity.getDiscountType()),
                entity.getDiscountValue(),
                entity.getEligibility(),
                entity.getApplicableProductIds(),
                entity.getMaxRedemptions(),
                entity.getCurrentRedemptions(),
                entity.getValidFrom(),
                entity.getValidUntil(),
                PromotionStatus.valueOf(entity.getStatus()),
                entity.getStripeCouponId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static PromotionPolicyEntity toEntity(PromotionPolicy domain) {
        var entity = new PromotionPolicyEntity();
        entity.setPolicyId(domain.getPolicyId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setDiscountType(domain.getDiscountType().name());
        entity.setDiscountValue(domain.getDiscountValue());
        entity.setEligibility(domain.getEligibility());
        entity.setApplicableProductIds(domain.getApplicableProductIds());
        entity.setMaxRedemptions(domain.getMaxRedemptions());
        entity.setCurrentRedemptions(domain.getCurrentRedemptions());
        entity.setValidFrom(domain.getValidFrom());
        entity.setValidUntil(domain.getValidUntil());
        entity.setStatus(domain.getStatus().name());
        entity.setStripeCouponId(domain.getStripeCouponId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
