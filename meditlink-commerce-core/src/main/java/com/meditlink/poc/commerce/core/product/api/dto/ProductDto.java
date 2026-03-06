package com.meditlink.poc.commerce.core.product.api.dto;

import java.util.List;
import java.util.Map;

public record ProductDto(
        String productId,
        String productGroupId,
        String externalId,
        String name,
        String description,
        String type,
        String billingType,
        String status,
        int displayOrder,
        Object condition,
        Map<String, Object> attributes,
        Map<String, Object> metadata,
        List<String> tags,
        List<ProductFeatureDto> features,
        List<PriceDto> prices
) {}
