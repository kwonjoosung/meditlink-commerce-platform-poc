package com.meditlink.poc.commerce.core.product.api.dto;

import java.util.List;
import java.util.Map;

public record PriceDto(
        String priceId,
        String productId,
        String externalId,
        String currency,
        long amount,
        String billingInterval,
        Integer intervalCount,
        boolean isDefault,
        Object condition,
        Map<String, Object> attributes,
        Map<String, Object> metadata,
        List<String> tags
) {}
