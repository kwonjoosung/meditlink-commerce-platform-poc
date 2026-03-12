package com.meditlink.poc.commerce.core.feature.api;

import com.meditlink.poc.commerce.core.feature.api.dto.FeatureDto;

import java.util.List;
import java.util.Optional;

/**
 * Feature BC의 모듈 간 인터페이스.
 * Product BC 등 다른 모듈에서 이 인터페이스를 통해 Feature 정보를 조회한다.
 */
public interface FeatureModuleApi {
    Optional<FeatureDto> getFeatureByCode(String featureCode);
    boolean existsByCode(String featureCode);
    List<FeatureDto> getFeaturesByCodes(List<String> featureCodes);
    List<FeatureDto> getAllFeatures();
}
