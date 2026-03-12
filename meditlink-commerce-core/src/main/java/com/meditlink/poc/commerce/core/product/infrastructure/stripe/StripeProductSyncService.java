package com.meditlink.poc.commerce.core.product.infrastructure.stripe;

import com.meditlink.poc.commerce.core.product.application.port.StripeProductSync;
import com.meditlink.poc.commerce.core.product.domain.product.Product;
import com.stripe.exception.StripeException;
import com.stripe.param.ProductCreateParams;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

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
                    .putMetadata("item_type", product.getItemType().name())
                    .build();

            var stripeProduct = com.stripe.model.Product.create(params);
            return stripeProduct.getId();
        } catch (StripeException e) {
            throw new RuntimeException("Stripe Product 동기화 실패: " + e.getMessage(), e);
        }
    }
}
