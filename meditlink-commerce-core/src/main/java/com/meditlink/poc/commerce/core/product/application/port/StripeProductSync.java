package com.meditlink.poc.commerce.core.product.application.port;

import com.meditlink.poc.commerce.core.product.domain.product.Product;

/**
 * Stripe Product 동기화 포트.
 * externalId를 할당한다.
 */
public interface StripeProductSync {
    String syncProduct(Product product);
}
