package com.meditlink.poc.commerce.core.shared.infra.rule;

import java.util.Map;

/**
 * Attribute 값 읽기 유틸.
 * JSONB attributes에서 값을 읽되, 없으면 AttributeDefinition의 default를 반환한다.
 */
public final class AttributeReader {

    private AttributeReader() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Map<String, Object> attributes, AttributeDefinition def) {
        if (attributes != null && attributes.containsKey(def.getKey())) {
            return (T) attributes.get(def.getKey());
        }
        return (T) def.getDefaultValue();
    }
}
