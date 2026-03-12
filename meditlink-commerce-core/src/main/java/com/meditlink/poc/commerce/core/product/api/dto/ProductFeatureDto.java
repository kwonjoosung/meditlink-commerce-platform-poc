package com.meditlink.poc.commerce.core.product.api.dto;

public record ProductFeatureDto(
        String productId,
        String featureCode,
        Long quota,
        String displayLabel,
        boolean isHighlighted
) {}
