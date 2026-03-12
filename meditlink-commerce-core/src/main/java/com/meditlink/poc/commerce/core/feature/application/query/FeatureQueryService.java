package com.meditlink.poc.commerce.core.feature.application.query;

import com.meditlink.poc.commerce.core.feature.application.port.FeatureRepository;
import com.meditlink.poc.commerce.core.feature.domain.Feature;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FeatureQueryService {

    private final FeatureRepository featureRepository;

    public FeatureQueryService(FeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    public Feature getFeature(UUID featureId) {
        return featureRepository.findById(featureId)
                .orElseThrow(() -> new IllegalArgumentException("Feature를 찾을 수 없습니다: " + featureId));
    }

    public Feature getFeatureByCode(String featureCode) {
        return featureRepository.findByCode(featureCode)
                .orElseThrow(() -> new IllegalArgumentException("Feature를 찾을 수 없습니다: " + featureCode));
    }

    public List<Feature> getAllFeatures() {
        return featureRepository.findAll();
    }

    public boolean existsByCode(String featureCode) {
        return featureRepository.existsByCode(featureCode);
    }
}
