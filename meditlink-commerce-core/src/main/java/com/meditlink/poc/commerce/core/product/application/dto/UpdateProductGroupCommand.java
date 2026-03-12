package com.meditlink.poc.commerce.core.product.application.dto;

import java.util.Map;

public record UpdateProductGroupCommand(
        String name,
        String slug,
        String description,
        int sortOrder,
        Map<String, Object> displayConfig,
        Map<String, Object> visibilityRules
) {}
