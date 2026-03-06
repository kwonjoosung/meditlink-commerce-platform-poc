package com.meditlink.poc.commerce.core.shared.infra.rule;

/**
 * Rule 평가 엔진.
 * 순수 함수로 구현되며, Rule + RuleContext를 받아 Boolean 결과를 반환한다.
 */
public final class RuleEngine {

    private RuleEngine() {
    }

    /**
     * Rule을 평가한다.
     *
     * @param rule    평가할 Rule (null이면 true)
     * @param context 평가 입력값
     * @return 조건 충족 여부
     */
    public static boolean evaluate(Rule rule, RuleContext context) {
        if (rule == null) return true;

        if (rule instanceof LeafRule leaf) {
            return evaluateLeaf(leaf, context);
        }

        if (rule instanceof CompositeRule composite) {
            return evaluateComposite(composite, context);
        }

        throw new IllegalArgumentException("알 수 없는 Rule 타입: " + rule.getClass());
    }

    private static boolean evaluateLeaf(LeafRule rule, RuleContext context) {
        Object contextValue = context.get(rule.field());

        // 필드가 context에 없을 때
        if (contextValue == null && !context.has(rule.field())) {
            return switch (rule.op()) {
                case "EXISTS" -> false;
                case "IS_EMPTY" -> true;
                default -> false;
            };
        }

        RuleOperator op = RuleOperator.valueOf(rule.op());
        return RuleOperators.apply(op, contextValue, rule.value());
    }

    private static boolean evaluateComposite(CompositeRule rule, RuleContext context) {
        return switch (rule.type()) {
            case "AND" -> rule.rules().stream().allMatch(r -> evaluate(r, context));
            case "OR" -> rule.rules().stream().anyMatch(r -> evaluate(r, context));
            case "NOT" -> !evaluate(rule.rules().getFirst(), context);
            default -> throw new IllegalArgumentException("알 수 없는 복합 타입: " + rule.type());
        };
    }
}
