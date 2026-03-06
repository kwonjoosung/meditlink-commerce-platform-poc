package com.meditlink.poc.commerce.core.shared.infra.rule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RuleEngineTest {

    @Test
    @DisplayName("null rule → true (조건 없음)")
    void evaluate_nullRule_returnsTrue() {
        RuleContext context = new RuleContext(Map.of());
        assertTrue(RuleEngine.evaluate(null, context));
    }

    @Nested
    @DisplayName("LeafRule 연산자")
    class LeafRuleTests {

        @Test
        void eq_sameValue_returnsTrue() {
            Rule rule = new LeafRule("region", "EQ", "US");
            RuleContext ctx = new RuleContext(Map.of("region", "US"));
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void eq_differentValue_returnsFalse() {
            Rule rule = new LeafRule("region", "EQ", "US");
            RuleContext ctx = new RuleContext(Map.of("region", "EU"));
            assertFalse(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void neq_differentValue_returnsTrue() {
            Rule rule = new LeafRule("region", "NEQ", "US");
            RuleContext ctx = new RuleContext(Map.of("region", "EU"));
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void gt_greaterValue_returnsTrue() {
            Rule rule = new LeafRule("subscription_months", "GT", 6);
            RuleContext ctx = new RuleContext(Map.of("subscription_months", 12));
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void gte_equalValue_returnsTrue() {
            Rule rule = new LeafRule("subscription_months", "GTE", 6);
            RuleContext ctx = new RuleContext(Map.of("subscription_months", 6));
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void lt_lessValue_returnsTrue() {
            Rule rule = new LeafRule("subscription_months", "LT", 6);
            RuleContext ctx = new RuleContext(Map.of("subscription_months", 3));
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void lte_equalValue_returnsTrue() {
            Rule rule = new LeafRule("subscription_months", "LTE", 6);
            RuleContext ctx = new RuleContext(Map.of("subscription_months", 6));
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void in_valueInList_returnsTrue() {
            Rule rule = new LeafRule("region", "IN", List.of("US", "EU"));
            RuleContext ctx = new RuleContext(Map.of("region", "US"));
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void notIn_valueNotInList_returnsTrue() {
            Rule rule = new LeafRule("region", "NOT_IN", List.of("US", "EU"));
            RuleContext ctx = new RuleContext(Map.of("region", "KR"));
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void contains_listContainsValue_returnsTrue() {
            Rule rule = new LeafRule("customer_tags", "CONTAINS", "enterprise");
            RuleContext ctx = new RuleContext(Map.of("customer_tags", List.of("enterprise", "vip")));
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void containsAny_hasIntersection_returnsTrue() {
            Rule rule = new LeafRule("active_products", "CONTAINS_ANY", List.of("pro", "enterprise"));
            RuleContext ctx = new RuleContext(Map.of("active_products", List.of("basic", "pro")));
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void containsAll_hasAll_returnsTrue() {
            Rule rule = new LeafRule("features", "CONTAINS_ALL", List.of("a", "b"));
            RuleContext ctx = new RuleContext(Map.of("features", List.of("a", "b", "c")));
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void containsAll_missing_returnsFalse() {
            Rule rule = new LeafRule("features", "CONTAINS_ALL", List.of("a", "d"));
            RuleContext ctx = new RuleContext(Map.of("features", List.of("a", "b", "c")));
            assertFalse(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void exists_fieldPresent_returnsTrue() {
            Rule rule = new LeafRule("custom_field", "EXISTS", true);
            RuleContext ctx = new RuleContext(Map.of("custom_field", "value"));
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void exists_fieldMissing_returnsFalse() {
            Rule rule = new LeafRule("custom_field", "EXISTS", true);
            RuleContext ctx = new RuleContext(Map.of());
            assertFalse(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void isEmpty_emptyList_returnsTrue() {
            Rule rule = new LeafRule("tags", "IS_EMPTY", true);
            RuleContext ctx = new RuleContext(Map.of("tags", List.of()));
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void isEmpty_nonEmptyString_returnsFalse() {
            Rule rule = new LeafRule("name", "IS_EMPTY", true);
            RuleContext ctx = new RuleContext(Map.of("name", "hello"));
            assertFalse(RuleEngine.evaluate(rule, ctx));
        }
    }

    @Nested
    @DisplayName("CompositeRule")
    class CompositeRuleTests {

        @Test
        void and_allMatch_returnsTrue() {
            Rule rule = new CompositeRule("AND", List.of(
                    new LeafRule("region", "EQ", "US"),
                    new LeafRule("subscription_months", "GTE", 6)
            ));
            RuleContext ctx = new RuleContext(Map.of("region", "US", "subscription_months", 12));
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void and_oneFails_returnsFalse() {
            Rule rule = new CompositeRule("AND", List.of(
                    new LeafRule("region", "EQ", "US"),
                    new LeafRule("subscription_months", "GTE", 6)
            ));
            RuleContext ctx = new RuleContext(Map.of("region", "US", "subscription_months", 3));
            assertFalse(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void or_oneMatches_returnsTrue() {
            Rule rule = new CompositeRule("OR", List.of(
                    new LeafRule("region", "EQ", "US"),
                    new LeafRule("region", "EQ", "EU")
            ));
            RuleContext ctx = new RuleContext(Map.of("region", "EU"));
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void or_noneMatch_returnsFalse() {
            Rule rule = new CompositeRule("OR", List.of(
                    new LeafRule("region", "EQ", "US"),
                    new LeafRule("region", "EQ", "EU")
            ));
            RuleContext ctx = new RuleContext(Map.of("region", "KR"));
            assertFalse(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void not_invertsTrueToFalse() {
            Rule rule = new CompositeRule("NOT", List.of(
                    new LeafRule("region", "EQ", "US")
            ));
            RuleContext ctx = new RuleContext(Map.of("region", "US"));
            assertFalse(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void not_invertsFalseToTrue() {
            Rule rule = new CompositeRule("NOT", List.of(
                    new LeafRule("region", "EQ", "US")
            ));
            RuleContext ctx = new RuleContext(Map.of("region", "EU"));
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }
    }

    @Nested
    @DisplayName("중첩 조건")
    class NestedRuleTests {

        @Test
        void nestedAndOr_evaluatesCorrectly() {
            // (region=US OR region=EU) AND subscription_months >= 6
            Rule rule = new CompositeRule("AND", List.of(
                    new CompositeRule("OR", List.of(
                            new LeafRule("region", "EQ", "US"),
                            new LeafRule("region", "EQ", "EU")
                    )),
                    new LeafRule("subscription_months", "GTE", 6)
            ));
            RuleContext ctx = new RuleContext(Map.of("region", "EU", "subscription_months", 12));
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }
    }

    @Nested
    @DisplayName("context에 필드 없을 때")
    class MissingFieldTests {

        @Test
        void missingField_eq_returnsFalse() {
            Rule rule = new LeafRule("region", "EQ", "US");
            RuleContext ctx = new RuleContext(Map.of());
            assertFalse(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void missingField_exists_returnsFalse() {
            Rule rule = new LeafRule("custom", "EXISTS", true);
            RuleContext ctx = new RuleContext(Map.of());
            assertFalse(RuleEngine.evaluate(rule, ctx));
        }

        @Test
        void missingField_isEmpty_returnsTrue() {
            Rule rule = new LeafRule("tags", "IS_EMPTY", true);
            RuleContext ctx = new RuleContext(Map.of());
            assertTrue(RuleEngine.evaluate(rule, ctx));
        }
    }
}
