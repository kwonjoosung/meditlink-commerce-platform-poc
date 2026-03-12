package com.meditlink.poc.commerce.core.product.api.dto;

import java.util.List;
import java.util.Map;

public record ProductGroupDto(
        String productGroupId,
        String slug,
        String name,
        String description,
        String type,
        String status,
        int sortOrder,
        Map<String, Object> displayConfig,
        Map<String, Object> visibilityRules,
        List<ProductDto> products
) {}
