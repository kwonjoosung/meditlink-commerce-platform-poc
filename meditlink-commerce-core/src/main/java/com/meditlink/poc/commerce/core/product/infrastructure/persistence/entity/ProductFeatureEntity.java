package com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.io.Serializable;
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

    @Column(name = "display_label")
    private String displayLabel;

    @Column(name = "is_highlighted", nullable = false)
    private boolean isHighlighted;

    @Column(name = "stripe_entitlement_id")
    private String stripeEntitlementId;

    public ProductFeatureEntity() {}

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getFeatureCode() { return featureCode; }
    public void setFeatureCode(String featureCode) { this.featureCode = featureCode; }
    public Long getQuota() { return quota; }
    public void setQuota(Long quota) { this.quota = quota; }
    public String getDisplayLabel() { return displayLabel; }
    public void setDisplayLabel(String displayLabel) { this.displayLabel = displayLabel; }
    public boolean isHighlighted() { return isHighlighted; }
    public void setHighlighted(boolean highlighted) { this.isHighlighted = highlighted; }
    public String getStripeEntitlementId() { return stripeEntitlementId; }
    public void setStripeEntitlementId(String stripeEntitlementId) { this.stripeEntitlementId = stripeEntitlementId; }

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
