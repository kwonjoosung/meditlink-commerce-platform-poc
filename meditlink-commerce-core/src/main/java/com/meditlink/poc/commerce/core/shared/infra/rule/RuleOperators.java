package com.meditlink.poc.commerce.core.shared.infra.rule;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 연산자별 평가 구현.
 */
public final class RuleOperators {

    private RuleOperators() {
    }

    public static boolean apply(RuleOperator op, Object contextValue, Object ruleValue) {
        return switch (op) {
            case EQ -> Objects.equals(contextValue, ruleValue);
            case NEQ -> !Objects.equals(contextValue, ruleValue);
            case GT -> compareNumbers(contextValue, ruleValue) > 0;
            case GTE -> compareNumbers(contextValue, ruleValue) >= 0;
            case LT -> compareNumbers(contextValue, ruleValue) < 0;
            case LTE -> compareNumbers(contextValue, ruleValue) <= 0;
            case IN -> asList(ruleValue).contains(contextValue);
            case NOT_IN -> !asList(ruleValue).contains(contextValue);
            case CONTAINS -> asList(contextValue).contains(ruleValue);
            case CONTAINS_ANY -> hasAnyIntersection(asList(contextValue), asList(ruleValue));
            case CONTAINS_ALL -> asList(contextValue).containsAll(asList(ruleValue));
            case EXISTS -> contextValue != null;
            case IS_EMPTY -> isEmpty(contextValue);
        };
    }

    private static int compareNumbers(Object a, Object b) {
        if (a instanceof Number na && b instanceof Number nb) {
            return Double.compare(na.doubleValue(), nb.doubleValue());
        }
        throw new IllegalArgumentException(
                "숫자 비교 연산에 숫자가 아닌 값이 전달됨: " + a + ", " + b);
    }

    @SuppressWarnings("unchecked")
    static List<Object> asList(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof List<?> list) {
            return (List<Object>) list;
        }
        if (value instanceof Collection<?> col) {
            return List.copyOf(col);
        }
        return List.of(value);
    }

    private static boolean hasAnyIntersection(List<Object> list1, List<Object> list2) {
        for (Object item : list2) {
            if (list1.contains(item)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEmpty(Object value) {
        if (value == null) return true;
        if (value instanceof String s) return s.isEmpty();
        if (value instanceof Collection<?> c) return c.isEmpty();
        return false;
    }
}
