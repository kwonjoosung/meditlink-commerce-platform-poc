package com.meditlink.poc.commerce.core.coupon.application.service;

import com.meditlink.poc.commerce.core.coupon.application.dto.IssueCouponFromPolicyCommand;
import com.meditlink.poc.commerce.core.coupon.application.port.CouponRepository;
import com.meditlink.poc.commerce.core.coupon.application.port.PromotionPolicyRepository;
import com.meditlink.poc.commerce.core.coupon.application.port.StripeCouponSync;
import com.meditlink.poc.commerce.core.coupon.domain.Coupon;
import com.meditlink.poc.commerce.core.coupon.domain.PromotionPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CouponService {

    private final CouponRepository couponRepository;
    private final PromotionPolicyRepository policyRepository;
    private final StripeCouponSync stripeCouponSync;

    public CouponService(CouponRepository couponRepository,
                         PromotionPolicyRepository policyRepository,
                         StripeCouponSync stripeCouponSync) {
        this.couponRepository = couponRepository;
        this.policyRepository = policyRepository;
        this.stripeCouponSync = stripeCouponSync;
    }

    /**
     * PromotionPolicy 기반으로 개별 쿠폰 발행
     */
    public Coupon issueFromPolicy(IssueCouponFromPolicyCommand cmd) {
        PromotionPolicy policy = policyRepository.findById(cmd.policyId())
                .orElseThrow(() -> new IllegalArgumentException("정책을 찾을 수 없습니다: " + cmd.policyId()));

        if (!policy.isApplicable(java.time.Instant.now())) {
            throw new IllegalStateException("현재 적용 불가능한 프로모션 정책입니다: " + cmd.policyId());
        }

        String code = generateCouponCode();
        Coupon coupon = Coupon.create(
                policy.getPolicyId(), code, cmd.customerId(),
                policy.getDiscountType(), policy.getDiscountValue(),
                policy.getValidUntil());

        // Stripe 쿠폰 ID 연결 (정책의 Stripe coupon 재사용)
        if (policy.getStripeCouponId() != null) {
            coupon.assignStripeCouponId(policy.getStripeCouponId());
        }

        policy.incrementRedemptions();
        policyRepository.save(policy);

        return couponRepository.save(coupon);
    }

    public Coupon redeem(UUID couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다: " + couponId));
        coupon.redeem();
        return couponRepository.save(coupon);
    }

    @Transactional(readOnly = true)
    public List<Coupon> findByCustomer(String customerId) {
        return couponRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Coupon> findByPolicy(UUID policyId) {
        return couponRepository.findByPolicyId(policyId);
    }

    private String generateCouponCode() {
        return "CPN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
