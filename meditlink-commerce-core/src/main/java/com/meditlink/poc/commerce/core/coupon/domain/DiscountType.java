package com.meditlink.poc.commerce.core.coupon.domain;

public enum DiscountType {
    PERCENTAGE,     // 비율 할인 (value = 20 → 20% 할인)
    FIXED_AMOUNT    // 고정 금액 할인 (value = 1000 → $10.00 할인, cents 단위)
}
