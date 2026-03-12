package com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_group_id")
    private UUID productGroupId;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "description")
    private String description;

    @Column(name = "item_type", nullable = false)
    private String itemType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "tier_order", nullable = false)
    private int tierOrder;

    @Column(name = "visibility", nullable = false)
    private String visibility;

    @Type(JsonType.class)
    @Column(name = "display_config", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> displayConfig;

    @Type(JsonType.class)
    @Column(name = "visibility_rules", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> visibilityRules;

    @Type(JsonType.class)
    @Column(name = "compatibility", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> compatibility;

    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;

    @OneToMany(mappedBy = "productId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductFeatureEntity> features = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ProductEntity() {}

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public UUID getProductGroupId() { return productGroupId; }
    public void setProductGroupId(UUID productGroupId) { this.productGroupId = productGroupId; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getTierOrder() { return tierOrder; }
    public void setTierOrder(int tierOrder) { this.tierOrder = tierOrder; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public Map<String, Object> getDisplayConfig() { return displayConfig; }
    public void setDisplayConfig(Map<String, Object> displayConfig) { this.displayConfig = displayConfig; }
    public Map<String, Object> getVisibilityRules() { return visibilityRules; }
    public void setVisibilityRules(Map<String, Object> visibilityRules) { this.visibilityRules = visibilityRules; }
    public Map<String, Object> getCompatibility() { return compatibility; }
    public void setCompatibility(Map<String, Object> compatibility) { this.compatibility = compatibility; }
    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }
    public List<ProductFeatureEntity> getFeatures() { return features; }
    public void setFeatures(List<ProductFeatureEntity> features) { this.features = features; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
