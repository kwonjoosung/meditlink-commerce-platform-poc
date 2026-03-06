package com.meditlink.poc.commerce.core.product.domain.price;

import com.meditlink.poc.commerce.core.shared.domain.PriceId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;
import com.meditlink.poc.commerce.core.shared.infra.rule.Rule;
import com.meditlink.poc.commerce.core.shared.infra.rule.RuleValidator;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Price Aggregate Root.
 * Product에 속하는 가격 정보.
 * Stripe Price는 금액 수정 불가 → 가격 변경 시 기존 비활성화 + 새 Price 생성.
 */
public class Price {

    private static final Set<String> ALLOWED_CURRENCIES = Set.of("USD", "EUR");

    private PriceId priceId;
    private ProductId productId;
    private String externalId;
    private String currency;
    private long amount;
    private String billingInterval;
    private Integer intervalCount;
    private boolean isDefault;
    private Rule condition;
    private Map<String, Object> attributes;
    private Map<String, Object> metadata;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;

    private Price() {}

    public static Price create(ProductId productId, String currency, long amount,
                               String billingInterval, Integer intervalCount, boolean isDefault) {
        Objects.requireNonNull(productId, "productId는 null일 수 없습니다");
        validateCurrency(currency);
        validateAmount(amount);

        var p = new Price();
        p.priceId = PriceId.generate();
        p.productId = productId;
        p.externalId = null; // Stripe 동기화 후 설정
        p.currency = currency;
        p.amount = amount;
        p.billingInterval = billingInterval;
        p.intervalCount = intervalCount;
        p.isDefault = isDefault;
        p.condition = null;
        p.attributes = Map.of();
        p.metadata = Map.of();
        p.tags = List.of();
        p.createdAt = Instant.now();
        p.updatedAt = Instant.now();
        return p;
    }

    public static Price reconstitute(
            PriceId priceId, ProductId productId, String externalId,
            String currency, long amount, String billingInterval, Integer intervalCount,
            boolean isDefault, Rule condition,
            Map<String, Object> attributes, Map<String, Object> metadata,
            List<String> tags, Instant createdAt, Instant updatedAt) {
        var p = new Price();
        p.priceId = priceId;
        p.productId = productId;
        p.externalId = externalId;
        p.currency = currency;
        p.amount = amount;
        p.billingInterval = billingInterval;
        p.intervalCount = intervalCount;
        p.isDefault = isDefault;
        p.condition = condition;
        p.attributes = attributes != null ? attributes : Map.of();
        p.metadata = metadata != null ? metadata : Map.of();
        p.tags = tags != null ? tags : List.of();
        p.createdAt = createdAt;
        p.updatedAt = updatedAt;
        return p;
    }

    // ── 도메인 메서드 ──

    public void assignExternalId(String externalId) {
        if (externalId == null || externalId.isBlank()) {
            throw new IllegalArgumentException("externalId는 비어 있을 수 없습니다");
        }
        this.externalId = externalId;
        this.updatedAt = Instant.now();
    }

    public void updateCondition(Rule condition) {
        if (condition != null) {
            var result = RuleValidator.validate(condition);
            if (!result.valid()) {
                throw new IllegalArgumentException("유효하지 않은 조건: " + result.errors());
            }
        }
        this.condition = condition;
        this.updatedAt = Instant.now();
    }

    public void updateAttributes(Map<String, Object> attributes) {
        this.attributes = attributes != null ? attributes : Map.of();
        this.updatedAt = Instant.now();
    }

    public void updateMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : Map.of();
        this.updatedAt = Instant.now();
    }

    public void updateTags(List<String> tags) {
        this.tags = tags != null ? tags : List.of();
        this.updatedAt = Instant.now();
    }

    public void markAsDefault() {
        this.isDefault = true;
        this.updatedAt = Instant.now();
    }

    public void unmarkAsDefault() {
        this.isDefault = false;
        this.updatedAt = Instant.now();
    }

    // ── Getters ──

    public PriceId getPriceId() { return priceId; }
    public ProductId getProductId() { return productId; }
    public String getExternalId() { return externalId; }
    public String getCurrency() { return currency; }
    public long getAmount() { return amount; }
    public String getBillingInterval() { return billingInterval; }
    public Integer getIntervalCount() { return intervalCount; }
    public boolean isDefault() { return isDefault; }
    public Rule getCondition() { return condition; }
    public Map<String, Object> getAttributes() { return attributes; }
    public Map<String, Object> getMetadata() { return metadata; }
    public List<String> getTags() { return tags; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    private static void validateCurrency(String currency) {
        if (currency == null || !ALLOWED_CURRENCIES.contains(currency)) {
            throw new IllegalArgumentException("허용되지 않은 통화: " + currency + " (허용: " + ALLOWED_CURRENCIES + ")");
        }
    }

    private static void validateAmount(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount는 0 이상이어야 합니다: " + amount);
        }
    }
}
