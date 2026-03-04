package com.meditlink.poc.commerce.core.coupon.adapter.in.web;

import com.meditlink.poc.commerce.core.coupon.application.port.in.IssueCouponCommand;
import com.meditlink.poc.commerce.core.coupon.application.port.in.IssueCouponUseCase;
import com.meditlink.poc.commerce.core.coupon.domain.Coupon;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
public class CouponCommandController {

    private final IssueCouponUseCase issueCouponUseCase;

    public CouponCommandController(IssueCouponUseCase issueCouponUseCase) {
        this.issueCouponUseCase = issueCouponUseCase;
    }

    @PostMapping
    public Coupon issue(@Valid @RequestBody IssueCouponRequest request) {
        return issueCouponUseCase.issue(new IssueCouponCommand(
                request.code(),
                request.discountRate(),
                request.expiresAt()
        ));
    }

    public record IssueCouponRequest(
            @NotBlank String code,
            @Min(0) @Max(100) int discountRate,
            Instant expiresAt
    ) {
    }
}
