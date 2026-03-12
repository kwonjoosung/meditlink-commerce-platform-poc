package com.meditlink.poc.commerce.core.feature.application.command;

import com.meditlink.poc.commerce.core.feature.application.dto.CreateFeatureCommand;
import com.meditlink.poc.commerce.core.feature.application.port.FeatureRepository;
import com.meditlink.poc.commerce.core.feature.domain.Feature;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class FeatureCommandService {

    private final FeatureRepository featureRepository;

    public FeatureCommandService(FeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    public Feature createFeature(CreateFeatureCommand cmd) {
        if (featureRepository.existsByCode(cmd.featureCode())) {
            throw new IllegalStateException("이미 존재하는 featureCode: " + cmd.featureCode());
        }
        Feature feature = Feature.create(cmd.featureCode(), cmd.name(), cmd.description(), cmd.type());
        return featureRepository.save(feature);
    }

    public Feature updateFeature(UUID featureId, String name, String description) {
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new IllegalArgumentException("Feature를 찾을 수 없습니다: " + featureId));
        feature.updateInfo(name, description);
        return featureRepository.save(feature);
    }

    public void deactivateFeature(UUID featureId) {
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new IllegalArgumentException("Feature를 찾을 수 없습니다: " + featureId));
        feature.deactivate();
        featureRepository.save(feature);
    }
}
