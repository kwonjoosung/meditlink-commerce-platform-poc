package com.meditlink.poc.commerce.integration.orchestration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateProductHttpRequest(
        @NotBlank String sku,
        @NotBlank String name,
        @PositiveOrZero long basePrice,
        @NotBlank String currency
) {
}
