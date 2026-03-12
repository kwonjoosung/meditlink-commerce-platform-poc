package com.meditlink.poc.commerce.core.coupon.application.dto;

import com.meditlink.poc.commerce.core.coupon.domain.DiscountType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreatePromotionPolicyCommand(
        String name, String description,
        DiscountType discountType, long discountValue,
        Map<String, Object> eligibility,
        List<UUID> applicableProductIds,
        Integer maxRedemptions,
        Instant validFrom, Instant validUntil) {}
