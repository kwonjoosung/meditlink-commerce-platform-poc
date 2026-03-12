package com.meditlink.poc.commerce.core.feature.infrastructure.persistence.repository;

import com.meditlink.poc.commerce.core.feature.infrastructure.persistence.entity.FeatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeatureJpaRepository extends JpaRepository<FeatureEntity, UUID> {
    Optional<FeatureEntity> findByFeatureCode(String featureCode);
    boolean existsByFeatureCode(String featureCode);
    List<FeatureEntity> findByFeatureCodeIn(List<String> featureCodes);
}
