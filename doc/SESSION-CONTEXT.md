# SESSION-CONTEXT.md — 세션 이어가기 파일

> 이 파일은 세션 간 컨텍스트를 전달하기 위한 파일이다.
> 새 세션에서 이 파일을 읽으면 이전 세션의 진행 상태를 파악할 수 있다.

---

## 현재 진행 상태

- **완료**: Step 1 (프로젝트 구조 전환 + 의존성 정비)
- **완료**: Step 2 (Shared 모듈 — Rule Engine + Attribute 시스템)
- **완료**: Step 3 (Product BC — Domain 모델)
- **완료**: Step 4 (Product BC — Persistence)
- **다음**: Step 5 (Product BC — Application Service)
- **전체 계획**: `doc/IMPLEMENTATION-PLAN.md` 참조

## 마지막 커밋

- Step 2 커밋 — feat: Step 2 - Rule Engine + Attribute 시스템 구현

## 핵심 결정사항

1. 패키지 루트: `com.meditlink.poc.commerce.core` 유지
2. 기존 코드 전부 제거 후 재작성 (coupon, gRPC 유지)
3. Domain ↔ JPA Entity 분리 (순수 도메인 + 별도 Entity + Mapper)
4. JSONB 매핑: Hypersistence Utils
5. Liquibase: 새로 시작 (기존 changeset 삭제)
6. **Catalog → ProductGroup 리네이밍** (INDEX.md, IMPLEMENTATION-PLAN.md에 매핑 기록)

## 참조해야 할 설계 문서

| 문서 | 용도 |
|------|------|
| `doc/IMPLEMENTATION-PLAN.md` | 전체 구현 계획, 패키지 구조, 결정사항 |
| `doc/INDEX.md` | 설계 문서 네비게이션 + 리네이밍 규칙 |
| `doc/RULE-ENGINE.md` | Step 2 구현 시 참조 (Rule 구조, 연산자, PriceSelector) |
| `doc/PRODUCT-BC.md` | Step 3 구현 시 참조 (도메인 모델, 불변식) |
| `doc/ARCHITECTURE.md` | 레이어 규칙, 의존성 방향 |

## Step 2 작업 요약

shared/ 패키지에 다음을 구현:
1. `shared/domain/` — ProductGroupId, ProductId, PriceId (UUID wrapper record)
2. `shared/infra/rule/` — Rule(sealed), CompositeRule, LeafRule, RuleOperator, RuleContext, RuleEngine, RuleOperators, RuleValidator, RuleDeserializer, PriceSelector
3. Attribute 시스템 — ProductGroupAttribute, ProductAttribute, PriceAttribute enum, AttributeReader
4. 테스트: RuleEngineTest, PriceSelectorTest, RuleValidatorTest, AttributeReaderTest

## 프로젝트 규칙 (CLAUDE.md 요약)

- 코드 외 모든 결과물은 한글
- 계획 먼저, 승인 없이 구현하지 않음
- 모르면 물어보기
- 작게 나누기
- 설계 문서와 충돌하면 먼저 논의
- 테스트 코드 함께 작성
