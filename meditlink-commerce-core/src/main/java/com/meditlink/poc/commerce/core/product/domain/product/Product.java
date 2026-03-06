package com.meditlink.poc.commerce.core.product.domain.product;

import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;
import com.meditlink.poc.commerce.core.shared.infra.rule.Rule;
import com.meditlink.poc.commerce.core.shared.infra.rule.RuleValidator;

import java.time.Instant;
import java.util.*;

/**
 * Product Aggregate Root.
 * ProductFeature를 내부 Entity로 관리한다.
 */
public class Product {

    private ProductId productId;
    private ProductGroupId productGroupId;
    private String externalId;
    private String name;
    private String description;
    private String type;
    private String billingType;
    private ProductStatus status;
    private int displayOrder;
    private Rule condition;
    private Map<String, Object> attributes;
    private Map<String, Object> metadata;
    private List<String> tags;
    private final List<ProductFeature> features = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    private Product() {}

    public static Product create(ProductGroupId productGroupId, String name, String description,
                                 String type, String billingType) {
        Objects.requireNonNull(productGroupId, "productGroupId는 null일 수 없습니다");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 비어 있을 수 없습니다");
        }
        validateBillingType(billingType);

        var p = new Product();
        p.productId = ProductId.generate();
        p.productGroupId = productGroupId;
        p.externalId = null; // Stripe 동기화 후 설정
        p.name = name;
        p.description = description;
        p.type = type;
        p.billingType = billingType;
        p.status = ProductStatus.ACTIVE;
        p.displayOrder = 0;
        p.condition = null;
        p.attributes = Map.of();
        p.metadata = Map.of();
        p.tags = List.of();
        p.createdAt = Instant.now();
        p.updatedAt = Instant.now();
        return p;
    }

    public static Product reconstitute(
            ProductId productId, ProductGroupId productGroupId, String externalId,
            String name, String description, String type, String billingType,
            ProductStatus status, int displayOrder, Rule condition,
            Map<String, Object> attributes, Map<String, Object> metadata,
            List<String> tags, List<ProductFeature> features,
            Instant createdAt, Instant updatedAt) {
        var p = new Product();
        p.productId = productId;
        p.productGroupId = productGroupId;
        p.externalId = externalId;
        p.name = name;
        p.description = description;
        p.type = type;
        p.billingType = billingType;
        p.status = status;
        p.displayOrder = displayOrder;
        p.condition = condition;
        p.attributes = attributes != null ? attributes : Map.of();
        p.metadata = metadata != null ? metadata : Map.of();
        p.tags = tags != null ? tags : List.of();
        if (features != null) {
            p.features.addAll(features);
        }
        p.createdAt = createdAt;
        p.updatedAt = updatedAt;
        return p;
    }

    // ── Feature 관리 (Aggregate Root가 내부 Entity를 관리) ──

    public void addFeature(String featureCode, Long quota, Map<String, Object> attrs) {
        if (featureCode == null || featureCode.isBlank()) {
            throw new IllegalArgumentException("featureCode는 비어 있을 수 없습니다");
        }
        boolean exists = features.stream().anyMatch(f -> f.getFeatureCode().equals(featureCode));
        if (exists) {
            throw new IllegalStateException("이미 존재하는 featureCode: " + featureCode);
        }
        features.add(new ProductFeature(this.productId, featureCode, quota, attrs));
        this.updatedAt = Instant.now();
    }

    public void removeFeature(String featureCode) {
        boolean removed = features.removeIf(f -> f.getFeatureCode().equals(featureCode));
        if (!removed) {
            throw new IllegalStateException("존재하지 않는 featureCode: " + featureCode);
        }
        this.updatedAt = Instant.now();
    }

    public Optional<ProductFeature> getFeature(String featureCode) {
        return features.stream()
                .filter(f -> f.getFeatureCode().equals(featureCode))
                .findFirst();
    }

    public List<ProductFeature> getFeatures() {
        return Collections.unmodifiableList(features);
    }

    // ── 도메인 메서드 ──

    public void assignExternalId(String externalId) {
        if (externalId == null || externalId.isBlank()) {
            throw new IllegalArgumentException("externalId는 비어 있을 수 없습니다");
        }
        this.externalId = externalId;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = ProductStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void updateInfo(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 비어 있을 수 없습니다");
        }
        this.name = name;
        this.description = description;
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

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
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

    public boolean isRecurring() {
        return "RECURRING".equals(billingType);
    }

    // ── Getters ──

    public ProductId getProductId() { return productId; }
    public ProductGroupId getProductGroupId() { return productGroupId; }
    public String getExternalId() { return externalId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getBillingType() { return billingType; }
    public ProductStatus getStatus() { return status; }
    public int getDisplayOrder() { return displayOrder; }
    public Rule getCondition() { return condition; }
    public Map<String, Object> getAttributes() { return attributes; }
    public Map<String, Object> getMetadata() { return metadata; }
    public List<String> getTags() { return tags; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    private static void validateBillingType(String billingType) {
        if (!"RECURRING".equals(billingType) && !"ONE_TIME".equals(billingType)) {
            throw new IllegalArgumentException("billingType은 RECURRING 또는 ONE_TIME이어야 합니다: " + billingType);
        }
    }
}
