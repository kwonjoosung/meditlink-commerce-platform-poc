package com.meditlink.poc.commerce.integration.orchestration.dto;

public record ProductPlanHttpResponse(
        String id,
        String productId,
        String groupId,
        String planCode,
        String planName,
        long price,
        String currency
) {
}
