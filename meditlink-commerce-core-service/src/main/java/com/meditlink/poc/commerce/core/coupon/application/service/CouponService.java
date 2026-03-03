package com.meditlink.poc.commerce.core.coupon.application.service;

import com.meditlink.poc.commerce.core.coupon.application.port.in.IssueCouponCommand;
import com.meditlink.poc.commerce.core.coupon.application.port.in.IssueCouponUseCase;
import com.meditlink.poc.commerce.core.coupon.domain.Coupon;
import org.springframework.stereotype.Service;

@Service
public class CouponService implements IssueCouponUseCase {

    @Override
    public Coupon issue(IssueCouponCommand command) {
        int normalizedDiscount = Math.max(0, Math.min(command.discountRate(), 100));
        return new Coupon(command.code(), normalizedDiscount, command.expiresAt());
    }
}
