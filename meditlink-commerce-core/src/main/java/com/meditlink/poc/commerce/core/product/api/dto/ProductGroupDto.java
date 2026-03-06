package com.meditlink.poc.commerce.core.product.api.dto;

import java.util.List;
import java.util.Map;

public record ProductGroupDto(
        String productGroupId,
        String slug,
        String name,
        String description,
        String status,
        int displayOrder,
        Object condition,
        Map<String, Object> attributes,
        Map<String, Object> metadata,
        List<String> tags,
        List<ProductDto> products
) {}
