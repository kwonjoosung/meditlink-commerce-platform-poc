package com.meditlink.poc.commerce.core.product.infrastructure.stripe;

import com.meditlink.poc.commerce.core.product.application.port.StripePriceSync;
import com.meditlink.poc.commerce.core.product.domain.price.Price;
import com.stripe.exception.StripeException;
import com.stripe.param.PriceCreateParams;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Stripe Price 동기화 구현체.
 * stripe 프로파일이 활성화될 때만 사용.
 */
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

            if (price.getBillingInterval() != null) {
                builder.setRecurring(
                        PriceCreateParams.Recurring.builder()
                                .setInterval(mapInterval(price.getBillingInterval()))
                                .setIntervalCount(price.getIntervalCount() != null ? price.getIntervalCount().longValue() : 1L)
                                .build()
                );
            }

            var stripePrice = com.stripe.model.Price.create(builder.build());
            return stripePrice.getId();
        } catch (StripeException e) {
            throw new RuntimeException("Stripe Price 동기화 실패: " + e.getMessage(), e);
        }
    }

    private PriceCreateParams.Recurring.Interval mapInterval(String billingInterval) {
        return switch (billingInterval.toUpperCase()) {
            case "MONTH" -> PriceCreateParams.Recurring.Interval.MONTH;
            case "YEAR" -> PriceCreateParams.Recurring.Interval.YEAR;
            case "WEEK" -> PriceCreateParams.Recurring.Interval.WEEK;
            case "DAY" -> PriceCreateParams.Recurring.Interval.DAY;
            default -> throw new IllegalArgumentException("알 수 없는 billing interval: " + billingInterval);
        };
    }
}
