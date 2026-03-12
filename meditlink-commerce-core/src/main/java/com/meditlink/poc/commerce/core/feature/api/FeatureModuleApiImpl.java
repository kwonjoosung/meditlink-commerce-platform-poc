package com.meditlink.poc.commerce.core.feature.api;

import com.meditlink.poc.commerce.core.feature.api.dto.FeatureDto;
import com.meditlink.poc.commerce.core.feature.application.port.FeatureRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FeatureModuleApiImpl implements FeatureModuleApi {

    private final FeatureRepository featureRepository;

    public FeatureModuleApiImpl(FeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    @Override
    public Optional<FeatureDto> getFeatureByCode(String featureCode) {
        return featureRepository.findByCode(featureCode)
                .map(f -> new FeatureDto(f.getFeatureId(), f.getFeatureCode(), f.getName(),
                        f.getDescription(), f.getType().name(), f.getStatus().name()));
    }

    @Override
    public boolean existsByCode(String featureCode) {
        return featureRepository.existsByCode(featureCode);
    }

    @Override
    public List<FeatureDto> getFeaturesByCodes(List<String> featureCodes) {
        return featureRepository.findByFeatureCodes(featureCodes).stream()
                .map(f -> new FeatureDto(f.getFeatureId(), f.getFeatureCode(), f.getName(),
                        f.getDescription(), f.getType().name(), f.getStatus().name()))
                .toList();
    }

    @Override
    public List<FeatureDto> getAllFeatures() {
        return featureRepository.findAll().stream()
                .map(f -> new FeatureDto(f.getFeatureId(), f.getFeatureCode(), f.getName(),
                        f.getDescription(), f.getType().name(), f.getStatus().name()))
                .toList();
    }
}
