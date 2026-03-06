package com.meditlink.poc.commerce.core.product.infrastructure.stripe;

import com.meditlink.poc.commerce.core.product.application.port.StripePriceSync;
import com.meditlink.poc.commerce.core.product.domain.price.Price;
import org.springframework.stereotype.Component;

/**
 * Stripe 동기화 스텁. Step 7에서 실제 구현으로 교체 예정.
 */
@Component
public class StubStripePriceSync implements StripePriceSync {

    @Override
    public String syncPrice(Price price, String productExternalId) {
        return "price_" + price.getPriceId().value().toString().substring(0, 8);
    }
}
