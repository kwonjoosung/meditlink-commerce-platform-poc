package com.meditlink.poc.commerce.core.feature.application.port;

import com.meditlink.poc.commerce.core.feature.domain.Feature;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeatureRepository {
    Feature save(Feature feature);
    Optional<Feature> findById(UUID featureId);
    Optional<Feature> findByCode(String featureCode);
    List<Feature> findAll();
    List<Feature> findByFeatureCodes(List<String> featureCodes);
    boolean existsByCode(String featureCode);
    void deleteById(UUID featureId);
}
