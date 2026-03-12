package com.meditlink.poc.commerce.core.feature.infrastructure.persistence.adapter;

import com.meditlink.poc.commerce.core.feature.application.port.FeatureRepository;
import com.meditlink.poc.commerce.core.feature.domain.Feature;
import com.meditlink.poc.commerce.core.feature.infrastructure.mapper.FeatureMapper;
import com.meditlink.poc.commerce.core.feature.infrastructure.persistence.repository.FeatureJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FeatureRepositoryImpl implements FeatureRepository {

    private final FeatureJpaRepository jpaRepository;

    public FeatureRepositoryImpl(FeatureJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Feature save(Feature feature) {
        var entity = FeatureMapper.toEntity(feature);
        var saved = jpaRepository.save(entity);
        return FeatureMapper.toDomain(saved);
    }

    @Override
    public Optional<Feature> findById(UUID featureId) {
        return jpaRepository.findById(featureId).map(FeatureMapper::toDomain);
    }

    @Override
    public Optional<Feature> findByCode(String featureCode) {
        return jpaRepository.findByFeatureCode(featureCode).map(FeatureMapper::toDomain);
    }

    @Override
    public List<Feature> findAll() {
        return jpaRepository.findAll().stream().map(FeatureMapper::toDomain).toList();
    }

    @Override
    public List<Feature> findByFeatureCodes(List<String> featureCodes) {
        return jpaRepository.findByFeatureCodeIn(featureCodes).stream().map(FeatureMapper::toDomain).toList();
    }

    @Override
    public boolean existsByCode(String featureCode) {
        return jpaRepository.existsByFeatureCode(featureCode);
    }

    @Override
    public void deleteById(UUID featureId) {
        jpaRepository.deleteById(featureId);
    }
}
