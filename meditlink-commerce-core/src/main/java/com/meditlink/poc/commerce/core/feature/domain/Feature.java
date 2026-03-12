package com.meditlink.poc.commerce.core.feature.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Feature Aggregate Root.
 * 시스템이 제공하는 기능을 정의한다.
 * Product BC의 ProductFeature.featureCode가 이 BC의 Feature를 참조.
 */
public class Feature {

    private UUID featureId;
    private String featureCode;
    private String name;
    private String description;
    private FeatureType type;
    private FeatureStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private Feature() {}

    public static Feature create(String featureCode, String name, String description, FeatureType type) {
        Objects.requireNonNull(featureCode, "featureCode는 null일 수 없습니다");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 비어 있을 수 없습니다");
        }
        var f = new Feature();
        f.featureId = UUID.randomUUID();
        f.featureCode = featureCode;
        f.name = name;
        f.description = description;
        f.type = type != null ? type : FeatureType.BOOLEAN;
        f.status = FeatureStatus.ACTIVE;
        f.createdAt = Instant.now();
        f.updatedAt = Instant.now();
        return f;
    }

    public static Feature reconstitute(UUID featureId, String featureCode, String name,
                                        String description, FeatureType type, FeatureStatus status,
                                        Instant createdAt, Instant updatedAt) {
        var f = new Feature();
        f.featureId = featureId;
        f.featureCode = featureCode;
        f.name = name;
        f.description = description;
        f.type = type;
        f.status = status;
        f.createdAt = createdAt;
        f.updatedAt = updatedAt;
        return f;
    }

    public void updateInfo(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 비어 있을 수 없습니다");
        }
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = FeatureStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = FeatureStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public UUID getFeatureId() { return featureId; }
    public String getFeatureCode() { return featureCode; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public FeatureType getType() { return type; }
    public FeatureStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
