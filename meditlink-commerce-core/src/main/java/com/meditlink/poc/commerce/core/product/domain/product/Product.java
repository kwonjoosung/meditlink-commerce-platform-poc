package com.meditlink.poc.commerce.core.product.domain.product;

import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;

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
    private String displayName;
    private String description;
    private ItemType itemType;
    private ProductStatus status;
    private int tierOrder;
    private Visibility visibility;
    private Map<String, Object> displayConfig;
    private Map<String, Object> visibilityRules;
    private Map<String, Object> compatibility;
    private List<String> tags;
    private final List<ProductFeature> features = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    private Product() {}

    public static Product create(ProductGroupId productGroupId, String name, String displayName,
                                 String description, ItemType itemType) {
        Objects.requireNonNull(productGroupId, "productGroupId는 null일 수 없습니다");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 비어 있을 수 없습니다");
        }
        Objects.requireNonNull(itemType, "itemType은 null일 수 없습니다");

        var p = new Product();
        p.productId = ProductId.generate();
        p.productGroupId = productGroupId;
        p.externalId = null;
        p.name = name;
        p.displayName = displayName;
        p.description = description;
        p.itemType = itemType;
        p.status = ProductStatus.ACTIVE;
        p.tierOrder = 0;
        p.visibility = Visibility.PUBLIC;
        p.displayConfig = Map.of();
        p.visibilityRules = Map.of();
        p.compatibility = Map.of();
        p.tags = List.of();
        p.createdAt = Instant.now();
        p.updatedAt = Instant.now();
        return p;
    }

    public static Product reconstitute(
            ProductId productId, ProductGroupId productGroupId, String externalId,
            String name, String displayName, String description, ItemType itemType,
            ProductStatus status, int tierOrder, Visibility visibility,
            Map<String, Object> displayConfig, Map<String, Object> visibilityRules,
            Map<String, Object> compatibility, List<String> tags,
            List<ProductFeature> features, Instant createdAt, Instant updatedAt) {
        var p = new Product();
        p.productId = productId;
        p.productGroupId = productGroupId;
        p.externalId = externalId;
        p.name = name;
        p.displayName = displayName;
        p.description = description;
        p.itemType = itemType;
        p.status = status;
        p.tierOrder = tierOrder;
        p.visibility = visibility != null ? visibility : Visibility.PUBLIC;
        p.displayConfig = displayConfig != null ? displayConfig : Map.of();
        p.visibilityRules = visibilityRules != null ? visibilityRules : Map.of();
        p.compatibility = compatibility != null ? compatibility : Map.of();
        p.tags = tags != null ? tags : List.of();
        if (features != null) {
            p.features.addAll(features);
        }
        p.createdAt = createdAt;
        p.updatedAt = updatedAt;
        return p;
    }

    // ── Feature 관리 ──

    public void addFeature(String featureCode, Long quota, String displayLabel, boolean isHighlighted) {
        if (featureCode == null || featureCode.isBlank()) {
            throw new IllegalArgumentException("featureCode는 비어 있을 수 없습니다");
        }
        boolean exists = features.stream().anyMatch(f -> f.getFeatureCode().equals(featureCode));
        if (exists) {
            throw new IllegalStateException("이미 존재하는 featureCode: " + featureCode);
        }
        features.add(new ProductFeature(this.productId, featureCode, quota, displayLabel, isHighlighted, null));
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

    public void updateInfo(String name, String displayName, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 비어 있을 수 없습니다");
        }
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void updateTierOrder(int tierOrder) {
        this.tierOrder = tierOrder;
        this.updatedAt = Instant.now();
    }

    public void updateVisibility(Visibility visibility) {
        Objects.requireNonNull(visibility, "visibility는 null일 수 없습니다");
        this.visibility = visibility;
        this.updatedAt = Instant.now();
    }

    public void updateDisplayConfig(Map<String, Object> displayConfig) {
        this.displayConfig = displayConfig != null ? displayConfig : Map.of();
        this.updatedAt = Instant.now();
    }

    public void updateVisibilityRules(Map<String, Object> visibilityRules) {
        this.visibilityRules = visibilityRules != null ? visibilityRules : Map.of();
        this.updatedAt = Instant.now();
    }

    public void updateCompatibility(Map<String, Object> compatibility) {
        this.compatibility = compatibility != null ? compatibility : Map.of();
        this.updatedAt = Instant.now();
    }

    public void updateTags(List<String> tags) {
        this.tags = tags != null ? tags : List.of();
        this.updatedAt = Instant.now();
    }

    public boolean isRecurring() {
        return itemType == ItemType.SUBSCRIPTION || itemType == ItemType.ADD_ON;
    }

    // ── Getters ──

    public ProductId getProductId() { return productId; }
    public ProductGroupId getProductGroupId() { return productGroupId; }
    public String getExternalId() { return externalId; }
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public ItemType getItemType() { return itemType; }
    public ProductStatus getStatus() { return status; }
    public int getTierOrder() { return tierOrder; }
    public Visibility getVisibility() { return visibility; }
    public Map<String, Object> getDisplayConfig() { return displayConfig; }
    public Map<String, Object> getVisibilityRules() { return visibilityRules; }
    public Map<String, Object> getCompatibility() { return compatibility; }
    public List<String> getTags() { return tags; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
