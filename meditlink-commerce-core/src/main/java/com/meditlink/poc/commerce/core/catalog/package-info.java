/**
 * Catalog 모듈
 * - Product 모듈의 공개 인터페이스(port-out/domain)만 참조하도록 제한
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Catalog",
        allowedDependencies = {"product::port-out", "product::domain"}
)
package com.meditlink.poc.commerce.core.catalog;
