package com.meditlink.poc.commerce.core.shared.infra.rule;

/**
 * 단일 조건. 하나의 필드를 하나의 연산자로 평가한다.
 *
 * @param field RuleContext에서 꺼낼 키
 * @param op    연산자 (RuleOperator enum name)
 * @param value 비교 대상 값
 */
public record LeafRule(
        String field,
        String op,
        Object value
) implements Rule {
}
