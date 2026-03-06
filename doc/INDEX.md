# 설계 문서 네비게이션

> 메인 규칙은 프로젝트 루트의 [CLAUDE.md](../CLAUDE.md) 참조.

---

## 설계 문서

| 문서                                                   | 내용 |
|------------------------------------------------------|------|
| [ARCHITECTURE.md](./ARCHITECTURE.md)                 | 모듈 구조, 패키지 규칙, 의존성 방향, DB 전략, 레이어 규칙 |
| [PRODUCT-BC.md](./PRODUCT-BC.md)                     | Product BC 상세 — 3 Aggregate, 도메인 모델, 불변식, DB 스키마, JSONB 규약, 예시 데이터 |
| [RULE-ENGINE.md](./RULE-ENGINE.md)                   | Rule 구조, 연산자, 평가 로직, Price 매칭 전략 (condition 우선 → attributes fallback) |
| [PRODUCT-API-SPEC.md](./PRODUCT-API-SPEC.md)         | Product 모듈 Public API — 모듈 내부 인터페이스 + REST 엔드포인트, DTO 정의 |
| [STRIPE-SYNC.md](./STRIPE-SYNC.md)                   | Stripe 동기화 — 방향, 매핑, 실패 처리, 불일치 감지, PG 교체 전략 |
| [IMPLEMENTATION-GUIDE.md](./IMPLEMENTATION-GUIDE.md) | 구현 순서 (8 Phase), 단계별 체크리스트, 테스트 전략, PoC 완료 기준 |

## 다이어그램

| 파일 | 내용 |
|------|------|
| [context-map.mermaid](./context-map.mermaid) | 전체 BC 간 관계도 (모듈러 모놀리스) |
| [product-bc.mermaid](./product-bc.mermaid) | Product BC ER 다이어그램 (3 Aggregate) |

---

## 실행 계획

| 파일 | 내용 |
|------|------|
| [IMPLEMENTATION-PLAN.md](./IMPLEMENTATION-PLAN.md) | 구현 계획 (실행용) — Gap 분석, Step별 작업, 결정사항, 진행 상태 |

---

## 메모 / 결정 사항

대화 중 결정된 사항은 이 디렉토리에 `.md` 파일로 저장.

| 파일 | 내용 |
|------|------|
| (아직 없음) | 구현 중 결정 사항이 생기면 여기에 추가 |

---

## 리네이밍 규칙

> ⚠️ 설계 문서에서 **Catalog**로 표기된 Aggregate는 구현 시 **ProductGroup**으로 리네이밍한다.
> 상세 매핑은 [IMPLEMENTATION-PLAN.md](./IMPLEMENTATION-PLAN.md)의 "리네이밍 매핑" 섹션 참조.
> 설계 문서 원본은 Catalog 표기를 유지하되, 이 규칙으로 해석한다.
