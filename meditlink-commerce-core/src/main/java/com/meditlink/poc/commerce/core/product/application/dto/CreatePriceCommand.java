package com.meditlink.poc.commerce.core.product.application.dto;

import java.util.List;
import java.util.Map;

public record CreatePriceCommand(
        String productId,
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
