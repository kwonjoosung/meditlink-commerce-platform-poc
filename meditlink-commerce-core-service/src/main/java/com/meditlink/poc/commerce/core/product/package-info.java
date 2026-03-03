/**
 * Product 모듈
 * - 상품 원장(Product), 상품 분류(Group), 상품 판매 플랜(Plan) 책임
 * - 외부에는 application port로 유스케이스를 노출하고, 구현은 adapter에서 담당
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Product"
)
package com.meditlink.poc.commerce.core.product;
