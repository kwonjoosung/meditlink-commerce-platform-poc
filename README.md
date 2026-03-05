# meditlink-commerce-platform-poc

학습용 PoC: `Gradle Multi Module + Spring Modulith + Hexagonal + gRPC + Jib + Docker Compose`

## 1. 모듈 구조

- `meditlink-commerce-common-proto`
: gRPC schema contract(`proto`) + generated stub 공유 모듈
- `meditlink-commerce-core`
: 핵심 도메인 서비스 (Modulith + Hexagonal, HTTP 8081 / gRPC 9090)
- `meditlink-commerce-client`
: 외부 채널/API 통합 계층 (`bff/orchestration/gateway/admin/webhook`, HTTP 8080)

## 2. DB 전략

- PostgreSQL 컨테이너 1개
- 데이터베이스 1개: `meditlink_commerce`
- 스키마 분리
1. `core` 스키마
2. `client` 스키마

초기 스키마 생성 SQL: [01-init-schemas.sql](/Users/medit/IdeaProjects/codex/commerce_test/docker/postgres/init/01-init-schemas.sql)

## 3. Jib Dockerizing (Apple Silicon + Linux 배포 대응)

- core 이미지: `meditlink/commerce-core:local`
- client 이미지: `meditlink/commerce-client:local`
- `Dockerfile` 없이 Gradle Jib 사용

아키텍처 제어:
- 로컬 Apple Silicon: `arm64` (기본)
- Linux 배포: `amd64` (속성 오버라이드)

직접 빌드:
```bash
# 로컬(기본 arm64)
./gradlew :meditlink-commerce-core:jibDockerBuild :meditlink-commerce-client:jibDockerBuild

# Linux 배포용 amd64 이미지
./gradlew :meditlink-commerce-core:jibDockerBuild :meditlink-commerce-client:jibDockerBuild -PjibTargetArch=amd64
```

## 4. 원클릭 배포 (docker compose)

- 실행 스크립트: [compose-up.sh](/Users/medit/IdeaProjects/codex/commerce_test/scripts/compose-up.sh)
- 종료 스크립트: [compose-down.sh](/Users/medit/IdeaProjects/codex/commerce_test/scripts/compose-down.sh)
- Linux 이미지 빌드 스크립트: [jib-build-linux.sh](/Users/medit/IdeaProjects/codex/commerce_test/scripts/jib-build-linux.sh)
- Compose 파일: [docker-compose.yml](/Users/medit/IdeaProjects/codex/commerce_test/docker-compose.yml)

실행:
```bash
./scripts/compose-up.sh
```

중지:
```bash
./scripts/compose-down.sh
```

볼륨까지 삭제:
```bash
./scripts/compose-down.sh --volumes
```

## 5. 샘플 호출 시나리오 (client 기준)

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

## 6. 다음 에이전트 시작 가이드

- 인수인계 문서: [NEXT_AGENT_HANDOFF.md](/Users/medit/IdeaProjects/codex/commerce_test/NEXT_AGENT_HANDOFF.md)
- 세션 메모리: [MEMORY_BANK.md](/Users/medit/IdeaProjects/codex/commerce_test/MEMORY_BANK.md)
- 부트스트랩 스크립트: [next-agent-bootstrap.sh](/Users/medit/IdeaProjects/codex/commerce_test/scripts/next-agent-bootstrap.sh)

시작 권장:
```bash
./scripts/next-agent-bootstrap.sh
```
