package com.meditlink.poc.commerce.core.coupon.application.port;

import com.meditlink.poc.commerce.core.coupon.domain.DiscountType;

public interface StripeCouponSync {
    String createCoupon(DiscountType type, long value, String name);
    void deleteCoupon(String stripeCouponId);
}
