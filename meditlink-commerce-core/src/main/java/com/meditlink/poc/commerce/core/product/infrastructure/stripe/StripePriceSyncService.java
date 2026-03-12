package com.meditlink.poc.commerce.core.product.infrastructure.stripe;

import com.meditlink.poc.commerce.core.product.application.port.StripePriceSync;
import com.meditlink.poc.commerce.core.product.domain.price.BillingPeriod;
import com.meditlink.poc.commerce.core.product.domain.price.Price;
import com.stripe.exception.StripeException;
import com.stripe.param.PriceCreateParams;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Primary
@Profile("stripe")
public class StripePriceSyncService implements StripePriceSync {

    @Override
    public String syncPrice(Price price, String productExternalId) {
        try {
            var builder = PriceCreateParams.builder()
                    .setProduct(productExternalId)
                    .setCurrency(price.getCurrency().toLowerCase())
                    .setUnitAmount(price.getAmount())
                    .putMetadata("internal_id", price.getPriceId().toString());

            if (price.getBillingPeriod() != BillingPeriod.ONE_TIME) {
                builder.setRecurring(
                        PriceCreateParams.Recurring.builder()
                                .setInterval(mapBillingPeriod(price.getBillingPeriod()))
                                .setIntervalCount(1L)
                                .build()
                );
            }

            var stripePrice = com.stripe.model.Price.create(builder.build());
            return stripePrice.getId();
        } catch (StripeException e) {
            throw new RuntimeException("Stripe Price 동기화 실패: " + e.getMessage(), e);
        }
    }

    private PriceCreateParams.Recurring.Interval mapBillingPeriod(BillingPeriod period) {
        return switch (period) {
            case MONTHLY -> PriceCreateParams.Recurring.Interval.MONTH;
            case YEARLY -> PriceCreateParams.Recurring.Interval.YEAR;
            case ONE_TIME -> throw new IllegalArgumentException("ONE_TIME은 recurring이 아닙니다");
        };
    }
}
