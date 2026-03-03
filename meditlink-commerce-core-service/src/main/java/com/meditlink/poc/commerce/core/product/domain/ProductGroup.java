package com.meditlink.poc.commerce.core.product.domain;

// 상품 그룹: 예) scanner-basic, scanner-premium 같은 상위 분류
public record ProductGroup(
        Long id,
        String code,
        String name
) {
}
