package com.meditlink.poc.commerce.core.product.application.dto;

public record AddFeatureCommand(
        String featureCode,
        Long quota,
        String displayLabel,
        boolean isHighlighted
) {}
