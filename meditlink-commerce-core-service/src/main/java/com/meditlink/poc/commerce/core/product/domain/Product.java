package com.meditlink.poc.commerce.core.product.domain;

public record Product(
        Long id,
        String sku,
        String name,
        long basePrice,
        String currency
) {

    public static Product newProduct(String sku, String name, long basePrice, String currency) {
        return new Product(null, sku, name, basePrice, currency);
    }
}
