package com.meditlink.poc.commerce.core.product.domain.product;

/**
 * 상품 노출 상태. Stripe 상태와 독립적으로 관리.
 */
public enum Visibility {
    PUBLIC,        // 전체 공개
    HIDDEN,        // 숨김 (기존 구독자 과금은 유지)
    SEGMENT_ONLY   // 특정 세그먼트에만 노출
}
