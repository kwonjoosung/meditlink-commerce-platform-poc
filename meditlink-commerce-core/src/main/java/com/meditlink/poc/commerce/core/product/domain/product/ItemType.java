package com.meditlink.poc.commerce.core.product.domain.product;

/**
 * 상품의 과금/결제 유형. Stripe 연동 방식을 결정하는 핵심 분기 축.
 */
public enum ItemType {
    SUBSCRIPTION,  // 정기 구독 (Stripe Subscription)
    ADD_ON,        // 추가 기능 (Subscription Item 추가)
    ONE_TIME       // 일회성 결제 (Checkout Session)
}
