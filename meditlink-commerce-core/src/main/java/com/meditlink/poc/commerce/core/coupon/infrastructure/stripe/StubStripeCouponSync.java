package com.meditlink.poc.commerce.core.coupon.infrastructure.stripe;

import com.meditlink.poc.commerce.core.coupon.application.port.StripeCouponSync;
import com.meditlink.poc.commerce.core.coupon.domain.DiscountType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Stripe Coupon 동기화 스텁 구현.
 * 실제 Stripe API 연동 전 테스트용.
 */
@Component
public class StubStripeCouponSync implements StripeCouponSync {

    private static final Logger log = LoggerFactory.getLogger(StubStripeCouponSync.class);

    @Override
    public String createCoupon(DiscountType type, long value, String name) {
        String stubId = "stub_cpn_" + UUID.randomUUID().toString().substring(0, 8);
        log.info("[STUB] Stripe Coupon 생성: type={}, value={}, name={}, stubId={}",
                type, value, name, stubId);
        return stubId;
    }

    @Override
    public void deleteCoupon(String stripeCouponId) {
        log.info("[STUB] Stripe Coupon 삭제: stripeCouponId={}", stripeCouponId);
    }
}
