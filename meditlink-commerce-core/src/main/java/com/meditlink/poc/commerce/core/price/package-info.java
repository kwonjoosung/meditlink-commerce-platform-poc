/**
 * Price 모듈
 * - Product 공개 인터페이스를 사용해 가격 계산 수행
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Price",
        allowedDependencies = {"product::port-out", "product::domain"}
)
package com.meditlink.poc.commerce.core.price;
