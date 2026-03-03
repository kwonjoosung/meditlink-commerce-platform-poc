# meditlink-commerce-platform-poc

학습용 PoC: `Gradle Multi Module + Spring Modulith + Hexagonal + gRPC`

## 1. 모듈 구조

- `meditlink-commerce-common`
: gRPC schema contract(`proto`)와 generated stub 공유 모듈
- `meditlink-commerce-core-service`
: 핵심 도메인 서비스 (Modulith + Hexagonal)
- `meditlink-commerce-integration-layer`
: 외부 채널/API 통합 계층 (`bff/orchestration/gateway/admin/webhook`)

## 2. 왜 멀티 모듈인가?

- `common`으로 계약(Contract)을 고정해 core/integration의 결합도를 낮춤
- core 도메인 변경과 integration 채널 변경을 분리 배포하기 쉬워짐
- 학습 관점에서 “계약 계층”과 “비즈니스 계층”의 책임 분리가 명확함

## 3. 왜 Spring Modulith인가?

- 하나의 배포 단위(모놀리스) 안에서 모듈 경계를 강제할 수 있음
- `package-info.java` + `@ApplicationModule`로 의존 허용 범위를 선언
- `ApplicationModules.verify()` 테스트로 아키텍처 회귀를 조기 탐지

## 4. 왜 Hexagonal(Port & Adapter)인가?

- 도메인/유스케이스를 외부 기술(JPA, HTTP, gRPC)과 분리
- `application.port.in/out`은 계약, `adapter.in/out`은 구현
- gRPC/REST/JPA 기술이 바뀌어도 핵심 유스케이스 영향 최소화

## 5. Product Group / Plan 모델

- `product_groups(id, code, name)`
- `product_plans(id, product_id, group_id, plan_code, plan_name, price, currency)`

## 6. 실행 전 준비

- JDK 25
- PostgreSQL 2개 DB
1. `meditlink_commerce`
2. `meditlink_integration`

기본 계정(샘플)
- username: `postgres`
- password: `postgres`

## 7. 실행 순서

1. `meditlink-commerce-core-service` 실행 (`8081`, gRPC `9090`)
2. `meditlink-commerce-integration-layer` 실행 (`8080`)

## 8. 샘플 호출 시나리오 (integration-layer 기준)

1. 상품 생성
```bash
curl -X POST http://localhost:8080/api/bff/products \
  -H 'Content-Type: application/json' \
  -d '{"sku":"P-100","name":"Intraoral Scanner","basePrice":1200000,"currency":"KRW"}'
```

2. 상품 조회
```bash
curl http://localhost:8080/api/bff/products/{productId}
```

3. 가격 계산
```bash
curl "http://localhost:8080/api/bff/products/{productId}/price?couponCode=SALE10"
```

4. 쿠폰 발급
```bash
curl -X POST http://localhost:8080/api/bff/coupons \
  -H 'Content-Type: application/json' \
  -d '{"code":"SALE10-APR","discountRate":10}'
```

5. Product Group 목록
```bash
curl http://localhost:8080/api/bff/product-groups
```

6. Product Plan 목록
```bash
curl http://localhost:8080/api/bff/products/{productId}/plans
```

## 9. 학습 포인트 추천 순서

1. `settings.gradle.kts` / 루트 `build.gradle.kts`
2. `common`의 `proto`와 generated stub
3. core의 `product` 모듈(`port` → `service` → `adapter`)
4. integration의 `gateway.grpc`와 `orchestration`
5. Liquibase changelog로 스키마 진화 방식 확인
