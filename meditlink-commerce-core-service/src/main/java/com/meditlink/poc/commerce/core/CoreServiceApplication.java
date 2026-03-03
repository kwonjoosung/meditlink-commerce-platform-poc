package com.meditlink.poc.commerce.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// core-service 진입점
// - product/catalog/price/coupon 모듈을 하나의 프로세스로 실행
// - Modulith로 내부 모듈 경계를 강제하며, 배포는 단일 아티팩트로 유지
@SpringBootApplication
public class CoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreServiceApplication.class, args);
    }
}
