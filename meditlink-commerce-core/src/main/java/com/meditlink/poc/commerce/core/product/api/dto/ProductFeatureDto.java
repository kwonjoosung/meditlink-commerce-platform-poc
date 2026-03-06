package com.meditlink.poc.commerce.core.product.api.dto;

import java.util.Map;

public record ProductFeatureDto(
        String productId,
        String featureCode,
        Long quota,
        Map<String, Object> attributes
) {}
