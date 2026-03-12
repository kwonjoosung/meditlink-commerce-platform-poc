package com.meditlink.poc.commerce.core.product.api.dto;

import java.util.List;
import java.util.Map;

public record ProductDto(
        String productId,
        String productGroupId,
        String externalId,
        String name,
        String displayName,
        String description,
        String itemType,
        String status,
        int tierOrder,
        String visibility,
        Map<String, Object> displayConfig,
        Map<String, Object> visibilityRules,
        Map<String, Object> compatibility,
        List<ProductFeatureDto> features,
        List<PriceDto> prices
) {}
