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
- 아키텍처 정책
1. 로컬 Apple Silicon: `arm64` 빌드
2. Linux 배포: `amd64` 빌드 (`-PjibTargetArch=amd64`)

## 6) 실행 기본값
- core HTTP: `8081`, core gRPC: `9090`
- client HTTP: `8080`
- DB: `meditlink_commerce` (schema `core`, `client`)

## 7) 다음 세션 시작 템플릿
1. `MEMORY_BANK.md` 확인
2. `NEXT_AGENT_HANDOFF.md` 확인
3. `./scripts/next-agent-bootstrap.sh` 실행
4. 사용자와 큰 그림/우선순위 합의
5. 상세 계획 제시 + 승인 후 작업
6. 변경 시 Memory Bank `변경 로그` 업데이트

## 8) 현재 검증 완료 상태
- `./scripts/compose-up.sh` 성공
- `docker compose ps -a` 기준 `postgres/core/client` 모두 `Up`
- 헬스체크
1. `http://localhost:8081/actuator/health` = `UP`
2. `http://localhost:8080/actuator/health` = `UP`
- E2E API
1. 상품 생성/조회 성공
2. 가격 계산 성공
3. 쿠폰 발급 성공
4. group/plan 조회는 현재 seed 데이터 없어 `[]`

## 9) 잔여 리스크 / 주의사항
- 로컬 compose는 기동 안정성을 위해 `SPRING_JPA_HIBERNATE_DDL_AUTO=update` 사용 중
- 배포 환경에서는 Liquibase-only 전략으로 전환 필요
- Apple Silicon 로컬은 `arm64` 기본, Linux 배포는 `amd64` 빌드 필요

## 10) 변경 로그(요약)
- 2026-03-03: 초기 스켈레톤 생성
- 2026-03-03: Boot 4.x 정합화, integration 상태 저장 추가, gRPC(price/coupon) 확장
- 2026-03-03: 학습용 주석 강화 + product group/plan(gRPC/REST/DDL) 확장
- 2026-03-04: 모듈명 리네이밍, Jib dockerizing, 단일 Postgres+스키마 분리, docker compose 원클릭 배포 추가
- 2026-03-05: Apple Silicon/ Linux 아키텍처 가변 Jib 빌드 지원 + compose 기동 검증
- 2026-03-05: 다음 에이전트 인수인계 문서/부트스트랩 스크립트 추가
