package com.meditlink.poc.commerce.core.shared.infra.rule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RuleValidatorTest {

    @Test
    @DisplayName("null rule은 유효")
    void validate_nullRule_isValid() {
        var result = RuleValidator.validate(null);
        assertTrue(result.valid());
    }

    @Test
    @DisplayName("정상 LeafRule은 유효")
    void validate_validLeafRule_isValid() {
        var result = RuleValidator.validate(new LeafRule("region", "EQ", "US"));
        assertTrue(result.valid());
    }

    @Test
    @DisplayName("field가 null인 LeafRule은 에러")
    void validate_nullField_hasError() {
        var result = RuleValidator.validate(new LeafRule(null, "EQ", "US"));
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("field")));
    }

    @Test
    @DisplayName("field가 빈 문자열인 LeafRule은 에러")
    void validate_blankField_hasError() {
        var result = RuleValidator.validate(new LeafRule("  ", "EQ", "US"));
        assertFalse(result.valid());
    }

    @Test
    @DisplayName("잘못된 연산자는 에러")
    void validate_invalidOperator_hasError() {
        var result = RuleValidator.validate(new LeafRule("region", "UNKNOWN", "US"));
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("UNKNOWN")));
    }

    @Test
    @DisplayName("정상 CompositeRule은 유효")
    void validate_validCompositeRule_isValid() {
        var rule = new CompositeRule("AND", List.of(
                new LeafRule("region", "EQ", "US"),
                new LeafRule("tier", "GTE", 2)
        ));
        var result = RuleValidator.validate(rule);
        assertTrue(result.valid());
    }

    @Test
    @DisplayName("빈 rules를 가진 CompositeRule은 에러")
    void validate_emptyRules_hasError() {
        var result = RuleValidator.validate(new CompositeRule("AND", List.of()));
        assertFalse(result.valid());
    }

    @Test
    @DisplayName("NOT에 2개 이상 rules는 에러")
    void validate_notWithMultipleRules_hasError() {
        var rule = new CompositeRule("NOT", List.of(
                new LeafRule("a", "EQ", 1),
                new LeafRule("b", "EQ", 2)
        ));
        var result = RuleValidator.validate(rule);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("NOT")));
    }

    @Test
    @DisplayName("잘못된 복합 타입은 에러")
    void validate_invalidCompositeType_hasError() {
        var result = RuleValidator.validate(new CompositeRule("XOR", List.of(
                new LeafRule("a", "EQ", 1)
        )));
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("XOR")));
    }

    @Test
    @DisplayName("깊이 초과 시 에러")
    void validate_exceedMaxDepth_hasError() {
        // 깊이 7 (MAX_DEPTH=5 초과)
        Rule rule = new LeafRule("a", "EQ", 1);
        for (int i = 0; i < 7; i++) {
            rule = new CompositeRule("AND", List.of(rule));
        }
        var result = RuleValidator.validate(rule);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("깊이")));
    }
}
