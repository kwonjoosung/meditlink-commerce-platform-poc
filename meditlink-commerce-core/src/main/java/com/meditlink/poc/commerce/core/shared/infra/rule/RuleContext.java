package com.meditlink.poc.commerce.core.shared.infra.rule;

import java.util.Collections;
import java.util.Map;

/**
 * Rule 평가 입력값.
 * BFF가 여러 모듈에서 데이터를 수집해 조립하는 불변 Map.
 */
public class RuleContext {

    private final Map<String, Object> data;

    public RuleContext(Map<String, Object> data) {
        this.data = Collections.unmodifiableMap(data);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }
}
