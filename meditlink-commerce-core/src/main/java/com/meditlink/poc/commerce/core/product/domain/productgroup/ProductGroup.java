package com.meditlink.poc.commerce.core.product.domain.productgroup;

import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;
import com.meditlink.poc.commerce.core.shared.infra.rule.Rule;
import com.meditlink.poc.commerce.core.shared.infra.rule.RuleValidator;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ProductGroup Aggregate Root.
 * 설계 문서의 Catalog에 해당한다.
 */
public class ProductGroup {

    private ProductGroupId productGroupId;
    private String slug;
    private String name;
    private String description;
    private ProductGroupStatus status;
    private int displayOrder;
    private Rule condition;
    private Map<String, Object> attributes;
    private Map<String, Object> metadata;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;

    private ProductGroup() {}

    public static ProductGroup create(String name, String slug, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 비어 있을 수 없습니다");
        }
        var pg = new ProductGroup();
        pg.productGroupId = ProductGroupId.generate();
        pg.slug = slug;
        pg.name = name;
        pg.description = description;
        pg.status = ProductGroupStatus.DRAFT;
        pg.displayOrder = 0;
        pg.condition = null;
        pg.attributes = Map.of();
        pg.metadata = Map.of();
        pg.tags = List.of();
        pg.createdAt = Instant.now();
        pg.updatedAt = Instant.now();
        return pg;
    }

    /**
     * 영속성 계층에서 복원할 때 사용하는 팩토리.
     */
    public static ProductGroup reconstitute(
            ProductGroupId productGroupId, String slug, String name, String description,
            ProductGroupStatus status, int displayOrder, Rule condition,
            Map<String, Object> attributes, Map<String, Object> metadata,
            List<String> tags, Instant createdAt, Instant updatedAt) {
        var pg = new ProductGroup();
        pg.productGroupId = productGroupId;
        pg.slug = slug;
        pg.name = name;
        pg.description = description;
        pg.status = status;
        pg.displayOrder = displayOrder;
        pg.condition = condition;
        pg.attributes = attributes != null ? attributes : Map.of();
        pg.metadata = metadata != null ? metadata : Map.of();
        pg.tags = tags != null ? tags : List.of();
        pg.createdAt = createdAt;
        pg.updatedAt = updatedAt;
        return pg;
    }

    // ── 도메인 메서드 ──

    public void activate() {
        // TODO: 정책 - 하위 상품이 하나 이상 있어야 활성화 가능? (PoC에서는 느슨하게)
        this.status = ProductGroupStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void archive() {
        // TODO: 정책 - 하위 상품 처리 정책 확정 필요
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

    public boolean isDraft() { return status == ProductGroupStatus.DRAFT; }
    public boolean isActive() { return status == ProductGroupStatus.ACTIVE; }

    // ── Getters ──

    public ProductGroupId getProductGroupId() { return productGroupId; }
    public String getSlug() { return slug; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ProductGroupStatus getStatus() { return status; }
    public int getDisplayOrder() { return displayOrder; }
    public Rule getCondition() { return condition; }
    public Map<String, Object> getAttributes() { return attributes; }
    public Map<String, Object> getMetadata() { return metadata; }
    public List<String> getTags() { return tags; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
