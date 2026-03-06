package com.meditlink.poc.commerce.core.shared.infra.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Rule 저장 시 구조 검증.
 */
public final class RuleValidator {

    private static final int MAX_DEPTH = 5;
    private static final Set<String> VALID_COMPOSITE_TYPES = Set.of("AND", "OR", "NOT");
    private static final Set<String> VALID_OPERATORS = Arrays.stream(RuleOperator.values())
            .map(Enum::name).collect(Collectors.toSet());

    private RuleValidator() {
    }

    public static ValidationResult validate(Rule rule) {
        List<String> errors = new ArrayList<>();
        validateRule(rule, 0, errors);
        return new ValidationResult(errors.isEmpty(), errors);
    }

    private static void validateRule(Rule rule, int depth, List<String> errors) {
        if (rule == null) return;

        if (depth > MAX_DEPTH) {
            errors.add("Rule 중첩 깊이가 최대값 " + MAX_DEPTH + "을 초과했습니다");
            return;
        }

        if (rule instanceof LeafRule leaf) {
            validateLeaf(leaf, errors);
        } else if (rule instanceof CompositeRule composite) {
            validateComposite(composite, depth, errors);
        }
    }

    private static void validateLeaf(LeafRule rule, List<String> errors) {
        if (rule.field() == null || rule.field().isBlank()) {
            errors.add("LeafRule field는 필수입니다");
        }
        if (rule.op() == null || !VALID_OPERATORS.contains(rule.op())) {
            errors.add("유효하지 않은 연산자: " + rule.op());
        }
    }

    private static void validateComposite(CompositeRule rule, int depth, List<String> errors) {
        if (!VALID_COMPOSITE_TYPES.contains(rule.type())) {
            errors.add("유효하지 않은 복합 타입: " + rule.type());
        }
        if (rule.rules() == null || rule.rules().isEmpty()) {
            errors.add("CompositeRule은 최소 하나의 하위 Rule이 필요합니다");
        }
        if ("NOT".equals(rule.type()) && rule.rules() != null && rule.rules().size() != 1) {
            errors.add("NOT Rule은 정확히 하나의 하위 Rule만 가져야 합니다");
        }
        if (rule.rules() != null) {
            for (Rule sub : rule.rules()) {
                validateRule(sub, depth + 1, errors);
            }
        }
    }

    public record ValidationResult(boolean valid, List<String> errors) {
    }
}
