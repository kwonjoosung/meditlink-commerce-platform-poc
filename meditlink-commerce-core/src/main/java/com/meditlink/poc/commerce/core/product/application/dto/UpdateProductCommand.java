package com.meditlink.poc.commerce.core.product.application.dto;

import java.util.List;
import java.util.Map;

public record UpdateProductCommand(
        String name,
        String description,
        Object condition,
        Map<String, Object> attributes,
        Map<String, Object> metadata,
        List<String> tags
) {}
