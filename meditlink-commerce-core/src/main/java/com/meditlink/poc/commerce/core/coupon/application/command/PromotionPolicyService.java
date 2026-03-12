package com.meditlink.poc.commerce.core.coupon.application.command;

import com.meditlink.poc.commerce.core.coupon.application.dto.CreatePromotionPolicyCommand;
import com.meditlink.poc.commerce.core.coupon.application.port.PromotionPolicyRepository;
import com.meditlink.poc.commerce.core.coupon.application.port.StripeCouponSync;
import com.meditlink.poc.commerce.core.coupon.domain.PromotionPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PromotionPolicyService {

    private final PromotionPolicyRepository policyRepository;
    private final StripeCouponSync stripeCouponSync;

    public PromotionPolicyService(PromotionPolicyRepository policyRepository, StripeCouponSync stripeCouponSync) {
        this.policyRepository = policyRepository;
        this.stripeCouponSync = stripeCouponSync;
    }

    public PromotionPolicy createPolicy(CreatePromotionPolicyCommand cmd) {
        PromotionPolicy policy = PromotionPolicy.create(
                cmd.name(), cmd.description(), cmd.discountType(), cmd.discountValue(),
                cmd.eligibility(), cmd.applicableProductIds(),
                cmd.maxRedemptions(), cmd.validFrom(), cmd.validUntil());

        String stripeCouponId = stripeCouponSync.createCoupon(
                policy.getDiscountType(), policy.getDiscountValue(), policy.getName());
        policy.assignStripeCouponId(stripeCouponId);

        return policyRepository.save(policy);
    }

    public List<PromotionPolicy> findApplicablePolicies(UUID productId) {
        return policyRepository.findAllActive().stream()
                .filter(p -> p.isApplicable(Instant.now()))
                .filter(p -> p.isApplicableToProduct(productId))
                .toList();
    }

    public void deactivatePolicy(UUID policyId) {
        PromotionPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("정책을 찾을 수 없습니다: " + policyId));
        policy.deactivate();
        policyRepository.save(policy);
    }
}
