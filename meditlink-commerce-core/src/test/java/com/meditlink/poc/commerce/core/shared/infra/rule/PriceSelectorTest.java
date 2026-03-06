package com.meditlink.poc.commerce.core.shared.infra.rule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PriceSelectorTest {

    // 테스트용 PriceCandidate 구현
    record TestPrice(
            String name,
            String currency,
            boolean isDefault,
            Rule condition,
            Map<String, Object> attributes
    ) implements PriceSelector.PriceCandidate {
        @Override public String getCurrency() { return currency; }
        @Override public boolean isDefault() { return isDefault; }
        @Override public Rule getCondition() { return condition; }
        @Override public Map<String, Object> getAttributes() { return attributes; }
    }

    @Test
    @DisplayName("default price만 있을 때 → default 반환")
    void selectPrice_onlyDefault_returnsDefault() {
        var defaultPrice = new TestPrice("default", "USD", true, null, Map.of());
        var ctx = new RuleContext(Map.of());

        Optional<TestPrice> result = PriceSelector.selectPrice(List.of(defaultPrice), "USD", ctx);

        assertTrue(result.isPresent());
        assertEquals("default", result.get().name());
    }

    @Test
    @DisplayName("condition 매칭되는 가격 반환")
    void selectPrice_conditionMatches_returnsConditionalPrice() {
        var defaultPrice = new TestPrice("default", "USD", true, null, Map.of());
        var enterprisePrice = new TestPrice("enterprise", "USD", false,
                new LeafRule("customer_tags", "CONTAINS", "enterprise"),
                Map.of("priority", 1));

        var ctx = new RuleContext(Map.of("customer_tags", List.of("enterprise", "vip")));

        Optional<TestPrice> result = PriceSelector.selectPrice(
                List.of(defaultPrice, enterprisePrice), "USD", ctx);

        assertTrue(result.isPresent());
        assertEquals("enterprise", result.get().name());
    }

    @Test
    @DisplayName("condition 불일치 시 default fallback")
    void selectPrice_conditionNotMatch_fallsBackToDefault() {
        var defaultPrice = new TestPrice("default", "USD", true, null, Map.of());
        var enterprisePrice = new TestPrice("enterprise", "USD", false,
                new LeafRule("customer_tags", "CONTAINS", "enterprise"),
                Map.of("priority", 1));

        var ctx = new RuleContext(Map.of("customer_tags", List.of("basic")));

        Optional<TestPrice> result = PriceSelector.selectPrice(
                List.of(defaultPrice, enterprisePrice), "USD", ctx);

        assertTrue(result.isPresent());
        assertEquals("default", result.get().name());
    }

    @Test
    @DisplayName("attributes 매칭 (condition 없을 때)")
    void selectPrice_attributesMatch_returnsMatchedPrice() {
        var defaultPrice = new TestPrice("default", "USD", true, null, Map.of());
        var regionPrice = new TestPrice("us-ent", "USD", false, null,
                Map.of("audience", "enterprise", "region", "US", "priority", 1));

        var ctx = new RuleContext(Map.of("audience", "enterprise", "region", "US"));

        Optional<TestPrice> result = PriceSelector.selectPrice(
                List.of(defaultPrice, regionPrice), "USD", ctx);

        assertTrue(result.isPresent());
        assertEquals("us-ent", result.get().name());
    }

    @Test
    @DisplayName("priority 오름차순으로 선택")
    void selectPrice_multipleMatches_selectsByPriority() {
        var defaultPrice = new TestPrice("default", "USD", true, null, Map.of());
        var lowPriority = new TestPrice("low", "USD", false,
                new LeafRule("customer_tags", "CONTAINS", "enterprise"),
                Map.of("priority", 10));
        var highPriority = new TestPrice("high", "USD", false,
                new LeafRule("customer_tags", "CONTAINS", "enterprise"),
                Map.of("priority", 1));

        var ctx = new RuleContext(Map.of("customer_tags", List.of("enterprise")));

        // highPriority가 priority=1로 먼저 매칭되어야 함
        Optional<TestPrice> result = PriceSelector.selectPrice(
                List.of(defaultPrice, lowPriority, highPriority), "USD", ctx);

        assertTrue(result.isPresent());
        assertEquals("high", result.get().name());
    }

    @Test
    @DisplayName("통화 필터링 — EUR 가격만 대상")
    void selectPrice_filtersByCurrency() {
        var usdPrice = new TestPrice("usd", "USD", true, null, Map.of());
        var eurPrice = new TestPrice("eur", "EUR", true, null, Map.of());

        var ctx = new RuleContext(Map.of());

        Optional<TestPrice> result = PriceSelector.selectPrice(
                List.of(usdPrice, eurPrice), "EUR", ctx);

        assertTrue(result.isPresent());
        assertEquals("eur", result.get().name());
    }

    @Test
    @DisplayName("매칭 없고 default도 없으면 empty")
    void selectPrice_noMatchNoDefault_returnsEmpty() {
        var conditionalPrice = new TestPrice("cond", "USD", false,
                new LeafRule("region", "EQ", "JP"), Map.of());

        var ctx = new RuleContext(Map.of("region", "US"));

        Optional<TestPrice> result = PriceSelector.selectPrice(
                List.of(conditionalPrice), "USD", ctx);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("nonMatchingKeys(priority, discount_reason)는 attributes 매칭에서 제외")
    void selectPrice_nonMatchingKeysIgnored() {
        var defaultPrice = new TestPrice("default", "USD", true, null, Map.of());
        // priority와 discount_reason만 있으면 → 조건 없음과 동일 → 무조건 매칭
        var discountPrice = new TestPrice("discount", "USD", false, null,
                Map.of("priority", 0, "discount_reason", "promo"));

        var ctx = new RuleContext(Map.of());

        Optional<TestPrice> result = PriceSelector.selectPrice(
                List.of(defaultPrice, discountPrice), "USD", ctx);

        assertTrue(result.isPresent());
        assertEquals("discount", result.get().name());
    }
}
