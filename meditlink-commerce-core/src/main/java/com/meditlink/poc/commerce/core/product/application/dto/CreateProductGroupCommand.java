package com.meditlink.poc.commerce.core.product.application.dto;

import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroupType;

public record CreateProductGroupCommand(
        String name,
        String slug,
        String description,
        ProductGroupType type
) {}
