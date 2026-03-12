package com.meditlink.poc.commerce.core.coupon.infrastructure.mapper;

import com.meditlink.poc.commerce.core.coupon.domain.Coupon;
import com.meditlink.poc.commerce.core.coupon.domain.CouponStatus;
import com.meditlink.poc.commerce.core.coupon.domain.DiscountType;
import com.meditlink.poc.commerce.core.coupon.infrastructure.persistence.entity.CouponEntity;

public final class CouponMapper {

    private CouponMapper() {}

    public static Coupon toDomain(CouponEntity entity) {
        return Coupon.reconstitute(
                entity.getCouponId(),
                entity.getPolicyId(),
                entity.getCode(),
                entity.getCustomerId(),
                DiscountType.valueOf(entity.getDiscountType()),
                entity.getDiscountValue(),
                CouponStatus.valueOf(entity.getStatus()),
                entity.getStripeCouponId(),
                entity.getExpiresAt(),
                entity.getRedeemedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static CouponEntity toEntity(Coupon domain) {
        var entity = new CouponEntity();
        entity.setCouponId(domain.getCouponId());
        entity.setPolicyId(domain.getPolicyId());
        entity.setCode(domain.getCode());
        entity.setCustomerId(domain.getCustomerId());
        entity.setDiscountType(domain.getDiscountType().name());
        entity.setDiscountValue(domain.getDiscountValue());
        entity.setStatus(domain.getStatus().name());
        entity.setStripeCouponId(domain.getStripeCouponId());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setRedeemedAt(domain.getRedeemedAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
