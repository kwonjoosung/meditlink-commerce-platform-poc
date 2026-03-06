package com.meditlink.poc.commerce.core.shared.infra.rule;

/**
 * Rule DSL의 최상위 타입.
 * JSONB에 저장된 조건을 표현하며, CompositeRule(AND/OR/NOT) 또는 LeafRule(단일 조건)이 될 수 있다.
 * null은 "조건 없음 = 항상 true"를 의미한다.
 */
public sealed interface Rule permits CompositeRule, LeafRule {
}
