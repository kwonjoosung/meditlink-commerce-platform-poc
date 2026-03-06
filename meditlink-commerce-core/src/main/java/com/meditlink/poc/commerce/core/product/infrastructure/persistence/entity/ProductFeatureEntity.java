package com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "product_features")
@IdClass(ProductFeatureEntity.ProductFeatureId.class)
public class ProductFeatureEntity {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Id
    @Column(name = "feature_code")
    private String featureCode;

    @Column(name = "quota")
    private Long quota;

    @Type(JsonType.class)
    @Column(name = "attributes", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> attributes;

    public ProductFeatureEntity() {}

    // ── Getters / Setters ──

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getFeatureCode() { return featureCode; }
    public void setFeatureCode(String featureCode) { this.featureCode = featureCode; }

    public Long getQuota() { return quota; }
    public void setQuota(Long quota) { this.quota = quota; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }

    // ── Composite PK ──

    public static class ProductFeatureId implements Serializable {
        private UUID productId;
        private String featureCode;

        public ProductFeatureId() {}

        public ProductFeatureId(UUID productId, String featureCode) {
            this.productId = productId;
            this.featureCode = featureCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ProductFeatureId that)) return false;
            return Objects.equals(productId, that.productId) && Objects.equals(featureCode, that.featureCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(productId, featureCode);
        }
    }
}
