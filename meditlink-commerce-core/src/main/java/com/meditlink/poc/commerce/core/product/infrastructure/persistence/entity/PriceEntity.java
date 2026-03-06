package com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "prices")
public class PriceEntity {

    @Id
    @Column(name = "price_id")
    private UUID priceId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "amount", nullable = false)
    private long amount;

    @Column(name = "billing_interval")
    private String billingInterval;

    @Column(name = "interval_count")
    private Integer intervalCount;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Type(JsonType.class)
    @Column(name = "condition", columnDefinition = "jsonb")
    private Map<String, Object> condition;

    @Type(JsonType.class)
    @Column(name = "attributes", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> attributes;

    @Type(JsonType.class)
    @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PriceEntity() {}

    // ── Getters / Setters ──

    public UUID getPriceId() { return priceId; }
    public void setPriceId(UUID priceId) { this.priceId = priceId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    public String getBillingInterval() { return billingInterval; }
    public void setBillingInterval(String billingInterval) { this.billingInterval = billingInterval; }

    public Integer getIntervalCount() { return intervalCount; }
    public void setIntervalCount(Integer intervalCount) { this.intervalCount = intervalCount; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public Map<String, Object> getCondition() { return condition; }
    public void setCondition(Map<String, Object> condition) { this.condition = condition; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
