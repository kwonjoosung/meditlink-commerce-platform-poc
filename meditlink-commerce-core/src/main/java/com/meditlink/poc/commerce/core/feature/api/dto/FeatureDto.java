package com.meditlink.poc.commerce.core.feature.api.dto;

import java.util.UUID;

public record FeatureDto(UUID featureId, String featureCode, String name,
                         String description, String type, String status) {}
