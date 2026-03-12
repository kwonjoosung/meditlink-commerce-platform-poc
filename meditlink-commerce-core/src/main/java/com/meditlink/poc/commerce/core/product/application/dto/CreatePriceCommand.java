package com.meditlink.poc.commerce.core.product.application.dto;

import com.meditlink.poc.commerce.core.product.domain.price.BillingPeriod;

public record CreatePriceCommand(
        String productId,
        String currency,
        long amount,
        BillingPeriod billingPeriod,
        boolean isDefault
) {}
