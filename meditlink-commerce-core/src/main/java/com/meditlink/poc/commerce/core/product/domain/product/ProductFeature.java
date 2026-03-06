package com.meditlink.poc.commerce.core.product.domain.product;

import com.meditlink.poc.commerce.core.shared.domain.ProductId;

import java.util.Map;
import java.util.Objects;

/**
 * Product의 내부 Entity.
 * Product를 통해서만 생성/삭제된다.
 * featureCode는 Feature BC의 ID를 참조하지만 FK 제약은 없다.
 */
public class ProductFeature {

    private final ProductId productId;
    private final String featureCode;
    private Long quota;
    private Map<String, Object> attributes;

    public ProductFeature(ProductId productId, String featureCode, Long quota, Map<String, Object> attributes) {
        Objects.requireNonNull(productId, "productId는 null일 수 없습니다");
        if (featureCode == null || featureCode.isBlank()) {
            throw new IllegalArgumentException("featureCode는 비어 있을 수 없습니다");
        }
        this.productId = productId;
        this.featureCode = featureCode;
        this.quota = quota;
        this.attributes = attributes != null ? attributes : Map.of();
    }

    public ProductId getProductId() { return productId; }
    public String getFeatureCode() { return featureCode; }
    public Long getQuota() { return quota; }
    public Map<String, Object> getAttributes() { return attributes; }

    void updateQuota(Long quota) {
        this.quota = quota;
    }

    void updateAttributes(Map<String, Object> attributes) {
        this.attributes = attributes != null ? attributes : Map.of();
    }
}
