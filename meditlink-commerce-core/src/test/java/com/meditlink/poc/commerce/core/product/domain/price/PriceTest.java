package com.meditlink.poc.commerce.core.product.domain.price;

import com.meditlink.poc.commerce.core.shared.domain.ProductId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PriceTest {

    private final ProductId productId = ProductId.generate();

    @Test
    @DisplayName("생성 시 기본값 설정")
    void create_setsDefaults() {
        var price = Price.create(productId, "USD", 1900, BillingPeriod.MONTHLY, true);

        assertNotNull(price.getPriceId());
        assertEquals(productId, price.getProductId());
        assertNull(price.getExternalId());
        assertEquals("USD", price.getCurrency());
        assertEquals(1900, price.getAmount());
        assertEquals(BillingPeriod.MONTHLY, price.getBillingPeriod());
        assertTrue(price.isDefault());
    }

    @Test
    @DisplayName("amount = 0 허용 (무료 가격)")
    void create_zeroAmount_allowed() {
        var price = Price.create(productId, "USD", 0, BillingPeriod.MONTHLY, false);
        assertEquals(0, price.getAmount());
    }

    @Test
    @DisplayName("음수 amount 불허")
    void create_negativeAmount_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> Price.create(productId, "USD", -1, BillingPeriod.MONTHLY, true));
    }

    @Test
    @DisplayName("허용되지 않은 통화 불허")
    void create_invalidCurrency_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> Price.create(productId, "KRW", 1000, BillingPeriod.MONTHLY, true));
    }

    @Test
    @DisplayName("null 통화 불허")
    void create_nullCurrency_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> Price.create(productId, null, 1000, BillingPeriod.MONTHLY, true));
    }

    @Test
    @DisplayName("null productId 불허")
    void create_nullProductId_throws() {
        assertThrows(NullPointerException.class,
                () -> Price.create(null, "USD", 1000, BillingPeriod.MONTHLY, true));
    }

    @Test
    @DisplayName("null billingPeriod 불허")
    void create_nullBillingPeriod_throws() {
        assertThrows(NullPointerException.class,
                () -> Price.create(productId, "USD", 1000, null, true));
    }

    @Test
    @DisplayName("externalId 할당")
    void assignExternalId() {
        var price = Price.create(productId, "USD", 1900, BillingPeriod.MONTHLY, true);
        price.assignExternalId("price_abc123");
        assertEquals("price_abc123", price.getExternalId());
    }

    @Test
    @DisplayName("externalId 빈 값 불허")
    void assignExternalId_blank_throws() {
        var price = Price.create(productId, "USD", 1900, BillingPeriod.MONTHLY, true);
        assertThrows(IllegalArgumentException.class,
                () -> price.assignExternalId(""));
    }

    @Test
    @DisplayName("default 플래그 토글")
    void defaultToggle() {
        var price = Price.create(productId, "USD", 1900, BillingPeriod.MONTHLY, false);
        assertFalse(price.isDefault());

        price.markAsDefault();
        assertTrue(price.isDefault());

        price.unmarkAsDefault();
        assertFalse(price.isDefault());
    }

    @Test
    @DisplayName("YEARLY BillingPeriod 생성")
    void create_yearlyBillingPeriod() {
        var price = Price.create(productId, "USD", 19900, BillingPeriod.YEARLY, true);
        assertEquals(BillingPeriod.YEARLY, price.getBillingPeriod());
    }

    @Test
    @DisplayName("ONE_TIME BillingPeriod 생성")
    void create_oneTimeBillingPeriod() {
        var price = Price.create(productId, "EUR", 5000, BillingPeriod.ONE_TIME, true);
        assertEquals(BillingPeriod.ONE_TIME, price.getBillingPeriod());
    }
}
