package com.meditlink.poc.commerce.core.product.application.dto;

import com.meditlink.poc.commerce.core.product.domain.product.Visibility;

import java.util.Map;

public record UpdateProductCommand(
        String name,
        String displayName,
        String description,
        int tierOrder,
        Visibility visibility,
        Map<String, Object> displayConfig,
        Map<String, Object> visibilityRules,
        Map<String, Object> compatibility
) {}
