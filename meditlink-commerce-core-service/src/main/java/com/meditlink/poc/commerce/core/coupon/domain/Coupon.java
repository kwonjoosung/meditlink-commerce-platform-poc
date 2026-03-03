package com.meditlink.poc.commerce.core.coupon.domain;

import java.time.Instant;

public record Coupon(
        String code,
        int discountRate,
        Instant expiresAt
) {
}
