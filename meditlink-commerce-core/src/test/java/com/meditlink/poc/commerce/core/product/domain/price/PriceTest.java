package com.meditlink.poc.commerce.core.product.domain.price;

import com.meditlink.poc.commerce.core.shared.domain.ProductId;
import com.meditlink.poc.commerce.core.shared.infra.rule.LeafRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PriceTest {

    private final ProductId productId = ProductId.generate();

    @Test
    @DisplayName("생성 시 기본값 설정")
    void create_setsDefaults() {
        var price = Price.create(productId, "USD", 1900, "MONTH", 1, true);

        assertNotNull(price.getPriceId());
        assertEquals(productId, price.getProductId());
        assertNull(price.getExternalId());
        assertEquals("USD", price.getCurrency());
        assertEquals(1900, price.getAmount());
        assertEquals("MONTH", price.getBillingInterval());
        assertEquals(1, price.getIntervalCount());
        assertTrue(price.isDefault());
        assertNull(price.getCondition());
        assertTrue(price.getAttributes().isEmpty());
    }

    @Test
    @DisplayName("amount = 0 허용 (무료 가격)")
    void create_zeroAmount_allowed() {
        var price = Price.create(productId, "USD", 0, "MONTH", 1, false);
        assertEquals(0, price.getAmount());
    }

    @Test
    @DisplayName("음수 amount 불허")
    void create_negativeAmount_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> Price.create(productId, "USD", -1, "MONTH", 1, true));
    }

    @Test
    @DisplayName("허용되지 않은 통화 불허")
    void create_invalidCurrency_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> Price.create(productId, "KRW", 1000, "MONTH", 1, true));
    }

    @Test
    @DisplayName("null 통화 불허")
    void create_nullCurrency_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> Price.create(productId, null, 1000, "MONTH", 1, true));
    }

    @Test
    @DisplayName("null productId 불허")
    void create_nullProductId_throws() {
        assertThrows(NullPointerException.class,
                () -> Price.create(null, "USD", 1000, "MONTH", 1, true));
    }

    @Test
    @DisplayName("externalId 할당")
    void assignExternalId() {
        var price = Price.create(productId, "USD", 1900, "MONTH", 1, true);
        price.assignExternalId("price_abc123");
        assertEquals("price_abc123", price.getExternalId());
    }

    @Test
    @DisplayName("externalId 빈 값 불허")
    void assignExternalId_blank_throws() {
        var price = Price.create(productId, "USD", 1900, "MONTH", 1, true);
        assertThrows(IllegalArgumentException.class,
                () -> price.assignExternalId(""));
    }

    @Test
    @DisplayName("condition 업데이트")
    void updateCondition() {
        var price = Price.create(productId, "USD", 3900, "MONTH", 1, false);
        price.updateCondition(new LeafRule("customer_tags", "CONTAINS", "enterprise"));
        assertNotNull(price.getCondition());

        price.updateCondition(null);
        assertNull(price.getCondition());
    }

    @Test
    @DisplayName("잘못된 condition 불허")
    void updateCondition_invalid_throws() {
        var price = Price.create(productId, "USD", 3900, "MONTH", 1, false);
        assertThrows(IllegalArgumentException.class,
                () -> price.updateCondition(new LeafRule(null, "EQ", "US")));
    }

    @Test
    @DisplayName("default 플래그 토글")
    void defaultToggle() {
        var price = Price.create(productId, "USD", 1900, "MONTH", 1, false);
        assertFalse(price.isDefault());

        price.markAsDefault();
        assertTrue(price.isDefault());

        price.unmarkAsDefault();
        assertFalse(price.isDefault());
    }

    @Test
    @DisplayName("attributes, metadata, tags 업데이트")
    void updateCollections() {
        var price = Price.create(productId, "USD", 1900, "MONTH", 1, true);

        price.updateAttributes(Map.of("priority", 1));
        assertEquals(1, price.getAttributes().get("priority"));

        price.updateMetadata(Map.of("note", "test"));
        assertEquals("test", price.getMetadata().get("note"));

        price.updateTags(List.of("promo"));
        assertEquals(1, price.getTags().size());
    }

    @Test
    @DisplayName("null 컬렉션은 빈 컬렉션으로 대체")
    void updateCollections_null_becomesEmpty() {
        var price = Price.create(productId, "USD", 1900, "MONTH", 1, true);
        price.updateAttributes(null);
        price.updateMetadata(null);
        price.updateTags(null);

        assertTrue(price.getAttributes().isEmpty());
        assertTrue(price.getMetadata().isEmpty());
        assertTrue(price.getTags().isEmpty());
    }

    @Test
    @DisplayName("ONE_TIME: billingInterval/intervalCount null 허용")
    void create_oneTime_nullInterval() {
        var price = Price.create(productId, "EUR", 5000, null, null, true);
        assertNull(price.getBillingInterval());
        assertNull(price.getIntervalCount());
    }
}
