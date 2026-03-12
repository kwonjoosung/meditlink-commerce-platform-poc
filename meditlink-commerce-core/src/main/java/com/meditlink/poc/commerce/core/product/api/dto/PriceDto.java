package com.meditlink.poc.commerce.core.product.api.dto;

public record PriceDto(
        String priceId,
        String productId,
        String externalId,
        String currency,
        long amount,
        String billingPeriod,
        boolean isDefault
) {}
