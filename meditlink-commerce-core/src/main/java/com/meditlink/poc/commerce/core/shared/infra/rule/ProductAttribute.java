package com.meditlink.poc.commerce.core.shared.infra.rule;

import java.util.List;

public enum ProductAttribute implements AttributeDefinition {
    TIER("tier", Integer.class, 0),
    EXCLUSIVE_WITH("exclusive_with", List.class, List.of()),
    IDEMPOTENT_BY_OUTPUT("idempotent_by_output", Boolean.class, false);

    private final String key;
    private final Class<?> type;
    private final Object defaultValue;

    ProductAttribute(String key, Class<?> type, Object defaultValue) {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @Override public String getKey() { return key; }
    @Override public Class<?> getType() { return type; }
    @Override public Object getDefaultValue() { return defaultValue; }
}
