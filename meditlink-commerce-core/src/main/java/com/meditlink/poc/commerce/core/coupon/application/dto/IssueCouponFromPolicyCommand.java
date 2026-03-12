package com.meditlink.poc.commerce.core.coupon.application.dto;

import java.util.UUID;

public record IssueCouponFromPolicyCommand(UUID policyId, String customerId) {}
