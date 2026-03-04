package com.meditlink.poc.commerce.core.coupon.application.port.in;

import java.time.Instant;

public record IssueCouponCommand(
        String code,
        int discountRate,
        Instant expiresAt
) {
}
