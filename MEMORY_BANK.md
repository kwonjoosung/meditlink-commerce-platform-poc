# Memory Bank (Codex 협업 기준)

## 1) 세션 고정 컨텍스트
- 프로젝트: `meditlink-commerce-platform-poc`
- 목적: 최신 스택 학습용 PoC
- 아키텍처: `Gradle Multi Module + Spring Modulith + Hexagonal + gRPC`
- 기준 패키지: `com.meditlink.poc.commerce`
- Java: `25`
- Gradle DSL: `Kotlin DSL (.kts)`

## 2) 모듈 책임 (최신)
- `meditlink-commerce-common-proto`
: gRPC Schema Contract(proto) + generated stub 공유
- `meditlink-commerce-core`
: Modulith 기반 핵심 도메인(`catalog/product/price/coupon`)
- `meditlink-commerce-client`
: `bff/orchestration/gateway/admin/webhook` 단일 앱 내 서브 패키지

## 3) 사용자 협업 룰(고정)
- 작업/실행 전 상세 계획 공유 후 승인 받고 진행
- 애매하거나 결정이 필요한 사항은 즉시 질문
- 임의 결정 최소화 (사용자 의사 우선)
- 학습 목적을 위해 코드/설정에 설명 주석을 충분히 유지

## 4) 아키텍처 규칙(고정)
- Modulith 모듈 경계는 `package-info.java`와 `ModulithStructureTest`로 유지
- core ↔ client 통신은 gRPC 유지
- gRPC contract 변경 시 common-proto → core/client 동시 반영
- DB 스키마 변경은 Liquibase changeSet으로만 수행

## 5) DB/배포 규칙(고정)
- PostgreSQL 컨테이너 1개 사용
- DB는 `meditlink_commerce` 1개
- 논리 분리는 스키마로 수행: `core`, `client`
- 컨테이너 이미지는 Jib로 빌드
- 원클릭 실행은 `./scripts/compose-up.sh` 사용

## 6) 실행 기본값
- core HTTP: `8081`, core gRPC: `9090`
- client HTTP: `8080`
- DB: `meditlink_commerce` (schema `core`, `client`)

## 7) 다음 세션 시작 템플릿
1. `MEMORY_BANK.md` 확인
2. 상세 실행 계획 제시
3. 사용자 승인 후 작업
4. 변경 시 Memory Bank `변경 로그` 업데이트

## 8) 변경 로그(요약)
- 2026-03-03: 초기 스켈레톤 생성
- 2026-03-03: Boot 4.x 정합화, integration 상태 저장 추가, gRPC(price/coupon) 확장
- 2026-03-03: 학습용 주석 강화 + product group/plan(gRPC/REST/DDL) 확장
- 2026-03-04: 모듈명 리네이밍, Jib dockerizing, 단일 Postgres+스키마 분리, docker compose 원클릭 배포 추가
