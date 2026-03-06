package com.meditlink.poc.commerce.core.shared.infra.rule;

public enum RuleOperator {
    // 비교
    EQ,
    NEQ,
    GT,
    GTE,
    LT,
    LTE,

    // 목록 포함
    IN,
    NOT_IN,

    // 리스트 연산
    CONTAINS,
    CONTAINS_ANY,
    CONTAINS_ALL,

    // 존재
    EXISTS,
    IS_EMPTY
}
