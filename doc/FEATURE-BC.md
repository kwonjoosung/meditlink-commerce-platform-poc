# Feature BC 설계 문서

## 개요

Feature BC는 플랫폼에서 제공하는 기능(Feature)을 독립적으로 관리하는 Bounded Context이다.
Product에 포함되는 기능을 정의하고, 다른 BC에서 Feature 정보를 조회할 수 있도록 Module API를 제공한다.

## 핵심 원칙

- **모듈 분리**: Product BC와 독립적으로 Feature를 관리
- **이벤트 기반 통신**: 향후 Feature 변경 시 이벤트 발행 (현재는 Module API 직접 호출)
- **BFF 조합**: Product + Feature 조합은 BFF 레이어에서 수행

## Aggregate

### Feature (Aggregate Root)

| 필드 | 타입 | 설명 |
|------|------|------|
| featureId | UUID | PK |
| featureCode | String | 고유 코드 (예: `analytics`, `api_access`) |
| name | String | 표시 이름 |
| description | String | 설명 |
| type | FeatureType | BOOLEAN, QUOTA, UNLIMITED |
| status | FeatureStatus | ACTIVE, INACTIVE |
| createdAt | Instant | 생성일시 |
| updatedAt | Instant | 수정일시 |

## Value Objects / Enums

- **FeatureType**: `BOOLEAN` (있음/없음), `QUOTA` (수량 제한), `UNLIMITED` (무제한)
- **FeatureStatus**: `ACTIVE`, `INACTIVE`

## 패키지 구조

```
coupon/
├── domain/
│   ├── Feature.java
│   ├── FeatureType.java
│   └── FeatureStatus.java
├── application/
│   ├── port/
│   │   └── FeatureRepository.java
│   ├── dto/
│   │   └── CreateFeatureCommand.java
│   ├── command/
│   │   └── FeatureCommandService.java
│   └── query/
│       └── FeatureQueryService.java
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/FeatureEntity.java
│   │   ├── repository/FeatureJpaRepository.java
│   │   └── adapter/FeatureRepositoryImpl.java
│   └── mapper/FeatureMapper.java
└── api/
    ├── FeatureModuleApi.java
    ├── FeatureModuleApiImpl.java
    └── dto/FeatureDto.java
```

## Module API

```java
public interface FeatureModuleApi {
    List<FeatureDto> findAll();
    Optional<FeatureDto> findByCode(String featureCode);
}
```

## DB 테이블

```sql
CREATE TABLE features (
    feature_id   UUID PRIMARY KEY,
    feature_code VARCHAR(100) NOT NULL UNIQUE,
    name         VARCHAR(200) NOT NULL,
    description  TEXT,
    type         VARCHAR(20)  NOT NULL,  -- BOOLEAN, QUOTA, UNLIMITED
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
```

## Product와의 관계

- Product.features는 `featureCode`로 Feature를 참조
- Product는 Feature의 존재 여부를 직접 검증하지 않음 (느슨한 결합)
- BFF에서 Product + Feature 정보를 조합하여 클라이언트에 전달
