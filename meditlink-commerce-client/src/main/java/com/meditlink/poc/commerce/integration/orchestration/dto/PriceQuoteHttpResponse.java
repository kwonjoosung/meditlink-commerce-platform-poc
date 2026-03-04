package com.meditlink.poc.commerce.integration.orchestration.dto;

public record PriceQuoteHttpResponse(
        String productId,
        long finalPrice,
        String currency,
        String appliedCouponCode,
        boolean found
) {
}
