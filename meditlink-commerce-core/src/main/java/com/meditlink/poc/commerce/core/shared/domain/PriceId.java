package com.meditlink.poc.commerce.core.shared.domain;

import java.util.Objects;
import java.util.UUID;

public record PriceId(UUID value) {

    public PriceId {
        Objects.requireNonNull(value, "PriceId는 null일 수 없습니다");
    }

    public static PriceId generate() {
        return new PriceId(UUID.randomUUID());
    }

    public static PriceId of(UUID value) {
        return new PriceId(value);
    }

    public static PriceId of(String value) {
        return new PriceId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
