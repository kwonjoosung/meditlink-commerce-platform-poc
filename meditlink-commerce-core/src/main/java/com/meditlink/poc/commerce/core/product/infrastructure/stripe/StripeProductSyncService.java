package com.meditlink.poc.commerce.core.product.infrastructure.stripe;

import com.meditlink.poc.commerce.core.product.application.port.StripeProductSync;
import com.meditlink.poc.commerce.core.product.domain.product.Product;
import com.stripe.exception.StripeException;
import com.stripe.param.ProductCreateParams;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Stripe Product 동기화 구현체.
 * stripe 프로파일이 활성화될 때만 사용. 그 외에는 Stub 사용.
 */
@Component
@Primary
@Profile("stripe")
public class StripeProductSyncService implements StripeProductSync {

    @Override
    public String syncProduct(Product product) {
        try {
            var params = ProductCreateParams.builder()
                    .setName(product.getName())
                    .setDescription(product.getDescription())
                    .putMetadata("internal_id", product.getProductId().toString())
                    .putMetadata("type", product.getType())
                    .putMetadata("billing_type", product.getBillingType())
                    .build();

            var stripeProduct = com.stripe.model.Product.create(params);
            return stripeProduct.getId();
        } catch (StripeException e) {
            throw new RuntimeException("Stripe Product 동기화 실패: " + e.getMessage(), e);
        }
    }
}
