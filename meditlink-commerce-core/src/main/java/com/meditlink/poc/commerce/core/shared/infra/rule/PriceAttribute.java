package com.meditlink.poc.commerce.core.shared.infra.rule;

public enum PriceAttribute implements AttributeDefinition {
    PRIORITY("priority", Integer.class, 99),
    AUDIENCE("audience", String.class, null),
    REGION("region", String.class, null),
    DISCOUNT_REASON("discount_reason", String.class, null);

    private final String key;
    private final Class<?> type;
    private final Object defaultValue;

    PriceAttribute(String key, Class<?> type, Object defaultValue) {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @Override public String getKey() { return key; }
    @Override public Class<?> getType() { return type; }
    @Override public Object getDefaultValue() { return defaultValue; }
}
