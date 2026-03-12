package com.meditlink.poc.commerce.core.feature.application.dto;

import com.meditlink.poc.commerce.core.feature.domain.FeatureType;

public record CreateFeatureCommand(String featureCode, String name, String description, FeatureType type) {}
