package com.meditlink.poc.commerce.core.product.infrastructure.persistence.adapter;

import com.meditlink.poc.commerce.core.product.application.port.PriceRepository;
import com.meditlink.poc.commerce.core.product.domain.price.Price;
import com.meditlink.poc.commerce.core.product.infrastructure.mapper.PriceMapper;
import com.meditlink.poc.commerce.core.product.infrastructure.persistence.repository.PriceJpaRepository;
import com.meditlink.poc.commerce.core.shared.domain.PriceId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PriceRepositoryImpl implements PriceRepository {

    private final PriceJpaRepository jpa;

    public PriceRepositoryImpl(PriceJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Price save(Price price) {
        var entity = PriceMapper.toEntity(price);
        var saved = jpa.save(entity);
        return PriceMapper.toDomain(saved);
    }

    @Override
    public Optional<Price> findById(PriceId id) {
        return jpa.findById(id.value()).map(PriceMapper::toDomain);
    }

    @Override
    public List<Price> findByProductId(ProductId productId) {
        return jpa.findByProductId(productId.value()).stream()
                .map(PriceMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(PriceId id) {
        jpa.deleteById(id.value());
    }
}
