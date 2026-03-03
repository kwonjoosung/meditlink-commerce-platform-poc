package com.meditlink.poc.commerce.core.catalog.domain;

public record CatalogItem(
        Long productId,
        String sku,
        String name
) {
}
