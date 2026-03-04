package com.meditlink.poc.commerce.core.coupon.application.port.in;

import com.meditlink.poc.commerce.core.coupon.domain.Coupon;

public interface IssueCouponUseCase {

    Coupon issue(IssueCouponCommand command);
}
