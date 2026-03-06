package com.meditlink.poc.commerce.core.product.application.dto;

import java.util.List;
import java.util.Map;

public record CreateProductCommand(
        String productGroupId,
        String name,
        String description,
        String type,
        String billingType,
        Object condition,
        Map<String, Object> attributes,
        Map<String, Object> metadata,
        List<String> tags
) {}
