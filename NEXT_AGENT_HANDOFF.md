# NEXT_AGENT_HANDOFF

## 1. 프로젝트 현재 상태
- 프로젝트명: `meditlink-commerce-platform-poc`
- 모듈
1. `meditlink-commerce-common-proto`
2. `meditlink-commerce-core`
3. `meditlink-commerce-client`
- 아키텍처: `Gradle Multi Module + Spring Modulith + Hexagonal + gRPC`
- 배포/실행: `Jib + Docker Compose`
- DB 전략: PostgreSQL 1개 + 스키마 분리(`core`, `client`)

## 2. 최근 핵심 커밋
- `d338e45` : 모듈명 리네이밍 + Jib/Compose 원클릭 배포 도입
- `bbf2cf7` : Apple Silicon/Jib 안정화 + compose 기동 안정화

## 3. 지금 바로 작업 시작 순서 (다음 에이전트용)
1. [MEMORY_BANK.md](/Users/medit/IdeaProjects/codex/commerce_test/MEMORY_BANK.md) 먼저 읽기
2. [README.md](/Users/medit/IdeaProjects/codex/commerce_test/README.md) 실행/배포 방식 확인
3. [scripts/next-agent-bootstrap.sh](/Users/medit/IdeaProjects/codex/commerce_test/scripts/next-agent-bootstrap.sh) 실행
4. 사용자와 다음 작업 목표/우선순위 합의

## 4. 로컬 검증 커맨드
```bash
# 원클릭 기동 (Apple Silicon: arm64 자동)
./scripts/compose-up.sh

# 상태 확인
docker compose ps -a

# 헬스체크
curl http://localhost:8081/actuator/health
curl http://localhost:8080/actuator/health

# 샘플 API
curl -X POST http://localhost:8080/api/bff/products \
  -H 'Content-Type: application/json' \
  -d '{"sku":"P-300","name":"Scanner","basePrice":1000000,"currency":"KRW"}'
```

## 5. 아키텍처/플랫폼 주의사항
- Jib Java 25 이슈 회피를 위해 `build.gradle.kts`에 `jib.container.mainClass`를 명시함
- `scripts/compose-up.sh`는 `uname -m` 기준 아키텍처를 자동 감지해 `-PjibTargetArch`를 전달함
- Linux 배포용 이미지는 `amd64`로 빌드
```bash
./scripts/jib-build-linux.sh
```
- compose 안정 기동을 위해 로컬 환경에서 `SPRING_JPA_HIBERNATE_DDL_AUTO=update` 오버라이드를 사용 중

## 6. 실패 이력/해결 요약
- 증상: `jibDockerBuild` 실패 (`Unsupported class file major version 69`)
  - 해결: `jib.container.mainClass` 명시 + packaged 모드 유지
- 증상: compose 기동 후 core/client 즉시 종료 (테이블 없음)
  - 해결: compose 환경변수로 `SPRING_JPA_HIBERNATE_DDL_AUTO=update`

## 7. 다음 작업 후보 (사용자와 합의 필요)
1. 로컬 `ddl-auto=update` 제거하고 Liquibase-only 시작 순서 안정화
2. product group/plan seed data changeSet 추가
3. compose에 healthcheck/readiness 추가
4. CI에서 `-PjibTargetArch=amd64` 빌드 파이프라인 고정

## 8. 작업 방식 가이드 (사용자 요청 반영)
- 작업 전 상세 계획 공유 후 승인받고 실행
- 애매한 결정은 즉시 질문
- 시니어 페어 프로그래밍처럼 큰 그림/우선순위를 먼저 맞추고 구현
