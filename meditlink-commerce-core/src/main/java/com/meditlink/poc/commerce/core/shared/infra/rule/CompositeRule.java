package com.meditlink.poc.commerce.core.shared.infra.rule;

import java.util.List;

/**
 * 복합 조건. 여러 Rule을 AND/OR/NOT으로 조합한다.
 *
 * @param type  "AND", "OR", "NOT"
 * @param rules 하위 Rule 목록 (재귀 구조)
 */
public record CompositeRule(
        String type,
        List<Rule> rules
) implements Rule {
}
