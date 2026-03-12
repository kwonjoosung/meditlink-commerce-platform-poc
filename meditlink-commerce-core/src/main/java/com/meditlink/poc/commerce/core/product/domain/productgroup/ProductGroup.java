package com.meditlink.poc.commerce.core.product.domain.productgroup;

import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ProductGroup Aggregate Root.
 * 상품 간의 구조적 관계를 정의하는 그룹.
 * type 필드로 용도를 구분한다 (plan_family, add_on_family, bundle).
 */
public class ProductGroup {

    private ProductGroupId productGroupId;
    private String slug;
    private String name;
    private String description;
    private ProductGroupType type;
    private ProductGroupStatus status;
    private int sortOrder;
    private Map<String, Object> displayConfig;
    private Map<String, Object> visibilityRules;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;

    private ProductGroup() {}

    public static ProductGroup create(String name, String slug, String description, ProductGroupType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 비어 있을 수 없습니다");
        }
        Objects.requireNonNull(type, "type은 null일 수 없습니다");

        var pg = new ProductGroup();
        pg.productGroupId = ProductGroupId.generate();
        pg.slug = slug;
        pg.name = name;
        pg.description = description;
        pg.type = type;
        pg.status = ProductGroupStatus.DRAFT;
        pg.sortOrder = 0;
        pg.displayConfig = Map.of();
        pg.visibilityRules = Map.of();
        pg.tags = List.of();
        pg.createdAt = Instant.now();
        pg.updatedAt = Instant.now();
        return pg;
    }

    public static ProductGroup reconstitute(
            ProductGroupId productGroupId, String slug, String name, String description,
            ProductGroupType type, ProductGroupStatus status, int sortOrder,
            Map<String, Object> displayConfig, Map<String, Object> visibilityRules,
            List<String> tags, Instant createdAt, Instant updatedAt) {
        var pg = new ProductGroup();
        pg.productGroupId = productGroupId;
        pg.slug = slug;
        pg.name = name;
        pg.description = description;
        pg.type = type;
        pg.status = status;
        pg.sortOrder = sortOrder;
        pg.displayConfig = displayConfig != null ? displayConfig : Map.of();
        pg.visibilityRules = visibilityRules != null ? visibilityRules : Map.of();
        pg.tags = tags != null ? tags : List.of();
        pg.createdAt = createdAt;
        pg.updatedAt = updatedAt;
        return pg;
    }

    // ── 도메인 메서드 ──

    public void activate() {
        this.status = ProductGroupStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void archive() {
        this.status = ProductGroupStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }

    public void reactivate() {
        if (this.status != ProductGroupStatus.ARCHIVED) {
            throw new IllegalStateException("ARCHIVED 상태에서만 재활성화할 수 있습니다");
        }
        this.status = ProductGroupStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void updateInfo(String name, String slug, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 비어 있을 수 없습니다");
        }
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void updateSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
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

    public void updateTags(List<String> tags) {
        this.tags = tags != null ? tags : List.of();
        this.updatedAt = Instant.now();
    }

    public boolean isDraft() { return status == ProductGroupStatus.DRAFT; }
    public boolean isActive() { return status == ProductGroupStatus.ACTIVE; }

    // ── Getters ──

    public ProductGroupId getProductGroupId() { return productGroupId; }
    public String getSlug() { return slug; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ProductGroupType getType() { return type; }
    public ProductGroupStatus getStatus() { return status; }
    public int getSortOrder() { return sortOrder; }
    public Map<String, Object> getDisplayConfig() { return displayConfig; }
    public Map<String, Object> getVisibilityRules() { return visibilityRules; }
    public List<String> getTags() { return tags; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
