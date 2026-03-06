package com.meditlink.poc.commerce.core.product.application.port;

import com.meditlink.poc.commerce.core.product.domain.price.Price;

/**
 * Stripe Price 동기화 포트.
 * externalId를 할당한다.
 */
public interface StripePriceSync {
    String syncPrice(Price price, String productExternalId);
}
