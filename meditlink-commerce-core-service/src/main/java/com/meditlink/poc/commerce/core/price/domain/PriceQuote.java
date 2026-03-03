package com.meditlink.poc.commerce.core.price.domain;

public record PriceQuote(
        Long productId,
        long finalPrice,
        String currency,
        String appliedCouponCode
) {
}
