package com.meditlink.poc.commerce.core.coupon.adapter.in.web;

import com.meditlink.poc.commerce.core.coupon.application.command.PromotionPolicyService;
import com.meditlink.poc.commerce.core.coupon.application.dto.CreatePromotionPolicyCommand;
import com.meditlink.poc.commerce.core.coupon.application.dto.IssueCouponFromPolicyCommand;
import com.meditlink.poc.commerce.core.coupon.application.service.CouponService;
import com.meditlink.poc.commerce.core.coupon.domain.Coupon;
import com.meditlink.poc.commerce.core.coupon.domain.DiscountType;
import com.meditlink.poc.commerce.core.coupon.domain.PromotionPolicy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/coupons")
public class CouponCommandController {

    private final PromotionPolicyService policyService;
    private final CouponService couponService;

    public CouponCommandController(PromotionPolicyService policyService, CouponService couponService) {
        this.policyService = policyService;
        this.couponService = couponService;
    }

    @PostMapping("/policies")
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionPolicy createPolicy(@Valid @RequestBody CreatePolicyRequest request) {
        var cmd = new CreatePromotionPolicyCommand(
                request.name(), request.description(),
                request.discountType(), request.discountValue(),
                request.eligibility(), request.applicableProductIds(),
                request.maxRedemptions(), request.validFrom(), request.validUntil());
        return policyService.createPolicy(cmd);
    }

    @PostMapping("/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public Coupon issueFromPolicy(@Valid @RequestBody IssueCouponRequest request) {
        var cmd = new IssueCouponFromPolicyCommand(request.policyId(), request.customerId());
        return couponService.issueFromPolicy(cmd);
    }

    @PostMapping("/{couponId}/redeem")
    public Coupon redeem(@PathVariable UUID couponId) {
        return couponService.redeem(couponId);
    }

    @GetMapping("/customer/{customerId}")
    public List<Coupon> findByCustomer(@PathVariable String customerId) {
        return couponService.findByCustomer(customerId);
    }

    public record CreatePolicyRequest(
            @NotBlank String name,
            String description,
            @NotNull DiscountType discountType,
            long discountValue,
            Map<String, Object> eligibility,
            List<UUID> applicableProductIds,
            Integer maxRedemptions,
            @NotNull Instant validFrom,
            Instant validUntil
    ) {}

    public record IssueCouponRequest(
            @NotNull UUID policyId,
            @NotBlank String customerId
    ) {}
}
