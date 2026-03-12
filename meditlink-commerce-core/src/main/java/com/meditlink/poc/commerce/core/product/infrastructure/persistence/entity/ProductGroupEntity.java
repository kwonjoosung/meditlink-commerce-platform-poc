package com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "product_groups")
public class ProductGroupEntity {

    @Id
    @Column(name = "product_group_id")
    private UUID productGroupId;

    @Column(name = "slug")
    private String slug;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Type(JsonType.class)
    @Column(name = "display_config", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> displayConfig;

    @Type(JsonType.class)
    @Column(name = "visibility_rules", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> visibilityRules;

    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ProductGroupEntity() {}

    public UUID getProductGroupId() { return productGroupId; }
    public void setProductGroupId(UUID productGroupId) { this.productGroupId = productGroupId; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public Map<String, Object> getDisplayConfig() { return displayConfig; }
    public void setDisplayConfig(Map<String, Object> displayConfig) { this.displayConfig = displayConfig; }
    public Map<String, Object> getVisibilityRules() { return visibilityRules; }
    public void setVisibilityRules(Map<String, Object> visibilityRules) { this.visibilityRules = visibilityRules; }
    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
