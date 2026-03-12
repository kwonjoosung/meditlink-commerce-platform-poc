package com.meditlink.poc.commerce.core.product.domain.price;

import com.meditlink.poc.commerce.core.shared.domain.PriceId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/**
 * Price Aggregate Root.
 * Product에 속하는 가격 정보. 정가(list price)만 관리한다.
 * 프로모션/할인은 Coupon BC가 담당 — Price를 오염시키지 않음.
 * Stripe Price는 금액 수정 불가 → 가격 변경 시 기존 비활성화 + 새 Price 생성.
 */
public class Price {

    private static final Set<String> ALLOWED_CURRENCIES = Set.of("USD", "EUR");

    private PriceId priceId;
    private ProductId productId;
    private String externalId;
    private String currency;
    private long amount;
    private BillingPeriod billingPeriod;
    private boolean isDefault;
    private Instant createdAt;
    private Instant updatedAt;

    private Price() {}

    public static Price create(ProductId productId, String currency, long amount,
                               BillingPeriod billingPeriod, boolean isDefault) {
        Objects.requireNonNull(productId, "productId는 null일 수 없습니다");
        Objects.requireNonNull(billingPeriod, "billingPeriod는 null일 수 없습니다");
        validateCurrency(currency);
        validateAmount(amount);

        var p = new Price();
        p.priceId = PriceId.generate();
        p.productId = productId;
        p.externalId = null;
        p.currency = currency;
        p.amount = amount;
        p.billingPeriod = billingPeriod;
        p.isDefault = isDefault;
        p.createdAt = Instant.now();
        p.updatedAt = Instant.now();
        return p;
    }

    public static Price reconstitute(
            PriceId priceId, ProductId productId, String externalId,
            String currency, long amount, BillingPeriod billingPeriod,
            boolean isDefault, Instant createdAt, Instant updatedAt) {
        var p = new Price();
        p.priceId = priceId;
        p.productId = productId;
        p.externalId = externalId;
        p.currency = currency;
        p.amount = amount;
        p.billingPeriod = billingPeriod;
        p.isDefault = isDefault;
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
    public BillingPeriod getBillingPeriod() { return billingPeriod; }
    public boolean isDefault() { return isDefault; }
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
