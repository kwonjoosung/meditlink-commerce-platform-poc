package com.meditlink.poc.commerce.core.product.domain.productgroup;

/**
 * ProductGroup의 용도를 구분하는 타입.
 */
public enum ProductGroupType {
    PLAN_FAMILY,     // 업/다운그레이드 경로를 정의하는 티어 라인
    ADD_ON_FAMILY,   // 같은 축의 Add-on 묶음
    BUNDLE           // 플랜 + 모듈 조합 패키지
}
