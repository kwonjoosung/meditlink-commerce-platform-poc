package com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.List;
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

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

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

    public ProductGroupEntity() {}

    // ── Getters / Setters ──

    public UUID getProductGroupId() { return productGroupId; }
    public void setProductGroupId(UUID productGroupId) { this.productGroupId = productGroupId; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

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
