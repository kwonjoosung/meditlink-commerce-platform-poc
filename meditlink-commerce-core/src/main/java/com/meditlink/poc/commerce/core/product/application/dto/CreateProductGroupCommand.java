package com.meditlink.poc.commerce.core.product.application.dto;

import java.util.List;
import java.util.Map;

public record CreateProductGroupCommand(
        String name,
        String slug,
        String description,
        Map<String, Object> attributes,
        Map<String, Object> metadata,
        List<String> tags
) {}
