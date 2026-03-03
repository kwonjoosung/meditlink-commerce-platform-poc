package com.meditlink.poc.commerce.core.product.domain;

// 상품 플랜: 제품별로 판매 정책/가격 단위를 정의
public record ProductPlan(
        Long id,
        Long productId,
        Long groupId,
        String planCode,
        String planName,
        long price,
        String currency
) {
}
