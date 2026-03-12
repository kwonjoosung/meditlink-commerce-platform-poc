package com.meditlink.poc.commerce.core.coupon.application.port;

import com.meditlink.poc.commerce.core.coupon.domain.Coupon;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponRepository {
    Coupon save(Coupon coupon);
    Optional<Coupon> findById(UUID couponId);
    Optional<Coupon> findByCode(String code);
    List<Coupon> findByCustomerId(String customerId);
    List<Coupon> findByPolicyId(UUID policyId);
}
