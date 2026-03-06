package com.meditlink.poc.commerce.core.shared.infra.rule;

public enum ProductGroupAttribute implements AttributeDefinition {
    PRODUCT_GROUP_TYPE("product_group_type", String.class, "general"),
    DISPLAY_STYLE("display_style", String.class, "list");

    private final String key;
    private final Class<?> type;
    private final Object defaultValue;

    ProductGroupAttribute(String key, Class<?> type, Object defaultValue) {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @Override public String getKey() { return key; }
    @Override public Class<?> getType() { return type; }
    @Override public Object getDefaultValue() { return defaultValue; }
}
