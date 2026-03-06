package com.meditlink.poc.commerce.core.shared.infra.rule;

/**
 * Attribute enum이 구현할 공통 인터페이스.
 * key, 타입, 기본값을 정의한다.
 */
public interface AttributeDefinition {
    String getKey();
    Class<?> getType();
    Object getDefaultValue();
}
