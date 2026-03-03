# meditlink-commerce-platform-poc

Java/Spring 기반 커머스 PoC 멀티모듈 스켈레톤입니다.

## Architecture

- `meditlink-commerce-common`
  - gRPC schema contract(`proto`) + generated stub 공유
  - 서비스: `ProductService`, `PriceService`, `CouponService`
- `meditlink-commerce-core-service`
  - Spring Modulith + Hexagonal(Port/Adapter)
  - 도메인 모듈: `catalog`, `product`, `price`, `coupon`
  - JPA + PostgreSQL + Liquibase
  - gRPC server 제공
- `meditlink-commerce-integration-layer`
  - 단일 앱 내부 서브패키지: `bff`, `orchestration`, `gateway`, `admin`, `webhook`
  - core-service와 gRPC 통신
  - JPA + PostgreSQL + Liquibase로 orchestration request 상태 저장

## Package base

- `com.meditlink.poc.commerce`

## Prerequisites

- JDK 25
- PostgreSQL (core-service, integration-layer 각각)

## Run order (개발 시 권장)

1. PostgreSQL 실행
2. `meditlink-commerce-core-service` 실행 (Liquibase 마이그레이션 + gRPC 9090 + HTTP 8081)
3. `meditlink-commerce-integration-layer` 실행 (Liquibase 마이그레이션 + HTTP 8080)

## Notes

- Java 25 Toolchain 고정
- Spring Boot 4.x / Spring Modulith / gRPC 조합 기준 PoC
- 보안(Spring Security)은 아직 미적용
