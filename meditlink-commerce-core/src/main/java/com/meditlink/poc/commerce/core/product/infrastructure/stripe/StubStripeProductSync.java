package com.meditlink.poc.commerce.core.product.infrastructure.stripe;

import com.meditlink.poc.commerce.core.product.application.port.StripeProductSync;
import com.meditlink.poc.commerce.core.product.domain.product.Product;
import org.springframework.stereotype.Component;

/**
 * Stripe 동기화 스텁. Step 7에서 실제 구현으로 교체 예정.
 */
@Component
public class StubStripeProductSync implements StripeProductSync {

    @Override
    public String syncProduct(Product product) {
        return "prod_" + product.getProductId().value().toString().substring(0, 8);
    }
}
