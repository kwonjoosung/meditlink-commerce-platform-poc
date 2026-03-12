package com.meditlink.poc.commerce.core.product.application.dto;

import com.meditlink.poc.commerce.core.product.domain.product.ItemType;

public record CreateProductCommand(
        String productGroupId,
        String name,
        String displayName,
        String description,
        ItemType itemType
) {}
