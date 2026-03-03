package com.meditlink.poc.commerce.integration.orchestration.dto;

public record ProductHttpResponse(
        String productId,
        String sku,
        String name,
        long basePrice,
        String currency,
        boolean found
) {
}
