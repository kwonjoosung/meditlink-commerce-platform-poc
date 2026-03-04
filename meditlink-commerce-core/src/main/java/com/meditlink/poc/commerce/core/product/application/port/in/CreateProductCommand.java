package com.meditlink.poc.commerce.core.product.application.port.in;

public record CreateProductCommand(
        String sku,
        String name,
        long basePrice,
        String currency
) {
}
