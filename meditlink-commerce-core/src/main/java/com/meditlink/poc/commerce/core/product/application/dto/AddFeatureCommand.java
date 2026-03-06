package com.meditlink.poc.commerce.core.product.application.dto;

import java.util.Map;

public record AddFeatureCommand(
        String featureCode,
        Long quota,
        Map<String, Object> attributes
) {}
