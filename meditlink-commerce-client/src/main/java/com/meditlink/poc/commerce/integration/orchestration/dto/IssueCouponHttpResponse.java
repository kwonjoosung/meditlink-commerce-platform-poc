package com.meditlink.poc.commerce.integration.orchestration.dto;

import java.time.Instant;

public record IssueCouponHttpResponse(
        String code,
        int discountRate,
        Instant expiresAt,
        boolean issued
) {
}
