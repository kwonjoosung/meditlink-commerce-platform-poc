package com.meditlink.poc.commerce.core.shared.domain;

import java.util.Objects;
import java.util.UUID;

public record ProductGroupId(UUID value) {

    public ProductGroupId {
        Objects.requireNonNull(value, "ProductGroupId는 null일 수 없습니다");
    }

    public static ProductGroupId generate() {
        return new ProductGroupId(UUID.randomUUID());
    }

    public static ProductGroupId of(UUID value) {
        return new ProductGroupId(value);
    }

    public static ProductGroupId of(String value) {
        return new ProductGroupId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
