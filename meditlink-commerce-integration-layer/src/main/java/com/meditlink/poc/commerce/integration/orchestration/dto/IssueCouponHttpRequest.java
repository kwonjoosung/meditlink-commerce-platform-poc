package com.meditlink.poc.commerce.integration.orchestration.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public record IssueCouponHttpRequest(
        @NotBlank String code,
        @Min(0) @Max(100) int discountRate,
        Instant expiresAt
) {
}
