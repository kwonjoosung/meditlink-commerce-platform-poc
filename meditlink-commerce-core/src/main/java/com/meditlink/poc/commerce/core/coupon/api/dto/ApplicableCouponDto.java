package com.meditlink.poc.commerce.core.coupon.api.dto;

import com.meditlink.poc.commerce.core.coupon.domain.DiscountType;

import java.util.UUID;

public record ApplicableCouponDto(
        UUID policyId,
        String name,
        String description,
        DiscountType discountType,
        long discountValue
) {}
