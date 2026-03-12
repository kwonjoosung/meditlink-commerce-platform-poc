package com.meditlink.poc.commerce.core.coupon.infrastructure.persistence.adapter;

import com.meditlink.poc.commerce.core.coupon.application.port.CouponRepository;
import com.meditlink.poc.commerce.core.coupon.domain.Coupon;
import com.meditlink.poc.commerce.core.coupon.infrastructure.mapper.CouponMapper;
import com.meditlink.poc.commerce.core.coupon.infrastructure.persistence.repository.CouponJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository jpa;

    public CouponRepositoryImpl(CouponJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Coupon save(Coupon coupon) {
        var entity = CouponMapper.toEntity(coupon);
        var saved = jpa.save(entity);
        return CouponMapper.toDomain(saved);
    }

    @Override
    public Optional<Coupon> findById(UUID couponId) {
        return jpa.findById(couponId).map(CouponMapper::toDomain);
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return jpa.findByCode(code).map(CouponMapper::toDomain);
    }

    @Override
    public List<Coupon> findByCustomerId(String customerId) {
        return jpa.findByCustomerId(customerId).stream()
                .map(CouponMapper::toDomain).toList();
    }

    @Override
    public List<Coupon> findByPolicyId(UUID policyId) {
        return jpa.findByPolicyId(policyId).stream()
                .map(CouponMapper::toDomain).toList();
    }
}
