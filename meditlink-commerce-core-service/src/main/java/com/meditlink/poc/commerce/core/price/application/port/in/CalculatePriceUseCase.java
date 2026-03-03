package com.meditlink.poc.commerce.core.price.application.port.in;

import com.meditlink.poc.commerce.core.price.domain.PriceQuote;

public interface CalculatePriceUseCase {

    PriceQuote calculate(Long productId, String couponCode);
}
