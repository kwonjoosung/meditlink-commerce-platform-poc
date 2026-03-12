<!-- Generated: 2026-03-06 | Updated: 2026-03-06 -->

# AGENTS.md — meditlink-commerce-platform-poc 루트 디렉토리

## Purpose

DDD 기반 모듈러 모놀리스 E-Commerce PoC 프로젝트. Gradle 멀티 모듈, Spring Boot 4.0, Spring Modulith, Hexagonal Architecture, gRPC, PostgreSQL, Stripe 통합을 학습 목적으로 구현한다.

**핵심 컨셉**: Bounded Context별 모듈 분리 (Product BC, Coupon BC 등), Domain-Entity 분리, Rule Engine 기반 가격 선택, Jib Dockerizing, Docker Compose 원클릭 배포.

**현재 구현 상태**: Product BC 전체 구현 완료 (ProductGroup, Product, Price Aggregate + Stripe 동기화 + 108개 테스트 통과). Coupon BC는 기존 코드 유지 (PoC 범위 외).

---

## Key Files

| File Path | Purpose |
|-----------|---------|
| `/Users/medit/IdeaProjects/codex/commerce_test/settings.gradle.kts` | Gradle 멀티 모듈 설정 (3개 모듈: common-proto, core, client) |
| `/Users/medit/IdeaProjects/codex/commerce_test/build.gradle.kts` | 루트 빌드 설정 (Java 25, UTF-8, Seoul 타임존) |
| `/Users/medit/IdeaProjects/codex/commerce_test/gradle/libs.versions.toml` | Version Catalog (Spring Boot 4.0, Modulith 2.0, gRPC, Stripe, Hypersistence Utils, ArchUnit) |
| `/Users/medit/IdeaProjects/codex/commerce_test/docker-compose.yml` | PostgreSQL + core + client 서비스 정의 (Jib 이미지 기반) |
| `/Users/medit/IdeaProjects/codex/commerce_test/CLAUDE.md` | AI 페어 개발 규칙 (계획 먼저, 모르면 물어보기, 작게 나누기) |
| `/Users/medit/IdeaProjects/codex/commerce_test/README.md` | 프로젝트 개요, 모듈 구조, 실행 방법, 샘플 API 호출 |
| `/Users/medit/IdeaProjects/codex/commerce_test/doc/IMPLEMENTATION-PLAN.md` | 전체 구현 계획 (Step 1~8), 결정사항, 패키지 구조, Catalog→ProductGroup 리네이밍 매핑 |
| `/Users/medit/IdeaProjects/codex/commerce_test/doc/SESSION-CONTEXT.md` | 세션 간 컨텍스트 전달 파일 (진행 상태, 커밋 이력, 핵심 결정사항) |
| `/Users/medit/IdeaProjects/codex/commerce_test/doc/ARCHITECTURE.md` | 아키텍처 원칙, 패키지 구조, 레이어 규칙, 의존성 방향, Domain-Entity 분리 전략 |
| `/Users/medit/IdeaProjects/codex/commerce_test/doc/PRODUCT-BC.md` | Product BC 도메인 모델, 3개 Aggregate (ProductGroup, Product, Price), 불변식, 생애주기 |
| `/Users/medit/IdeaProjects/codex/commerce_test/doc/RULE-ENGINE.md` | Rule Engine 설계 (CompositeRule, LeafRule), 연산자, PriceSelector 알고리즘 |
| `/Users/medit/IdeaProjects/codex/commerce_test/doc/STRIPE-SYNC.md` | Stripe 동기화 전략 (Product/Price 생성 시점, 멱등성, 에러 처리) |
| `/Users/medit/IdeaProjects/codex/commerce_test/doc/PRODUCT-API-SPEC.md` | REST API 명세 (ProductGroup, Product, Price CRUD) |
| `/Users/medit/IdeaProjects/codex/commerce_test/doc/INDEX.md` | 설계 문서 네비게이션 + Catalog→ProductGroup 리네이밍 규칙 |

---

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `/Users/medit/IdeaProjects/codex/commerce_test/meditlink-commerce-core/` | 핵심 도메인 서비스 (Product BC, Coupon BC, HTTP 8081, gRPC 9090) |
| `/Users/medit/IdeaProjects/codex/commerce_test/meditlink-commerce-client/` | 외부 채널/API 통합 계층 (BFF, Orchestration, Gateway, Admin, Webhook, HTTP 8080) |
| `/Users/medit/IdeaProjects/codex/commerce_test/meditlink-commerce-common-proto/` | gRPC schema contract (proto) + generated stub 공유 모듈 |
| `/Users/medit/IdeaProjects/codex/commerce_test/doc/` | 설계 문서 (ARCHITECTURE, PRODUCT-BC, RULE-ENGINE, STRIPE-SYNC, IMPLEMENTATION-PLAN 등) |
| `/Users/medit/IdeaProjects/codex/commerce_test/docker/` | Docker 관련 파일 (PostgreSQL init SQL) |
| `/Users/medit/IdeaProjects/codex/commerce_test/http/` | HTTP 요청 예시 파일 (IntelliJ HTTP Client 형식) |
| `/Users/medit/IdeaProjects/codex/commerce_test/scripts/` | 실행 스크립트 (compose-up.sh, compose-down.sh, jib-build-linux.sh, next-agent-bootstrap.sh) |
| `/Users/medit/IdeaProjects/codex/commerce_test/gradle/` | Gradle wrapper + libs.versions.toml (Version Catalog) |
| `/Users/medit/IdeaProjects/codex/commerce_test/.omc/` | OMC (oh-my-claudecode) 상태 관리 디렉토리 |
| `/Users/medit/IdeaProjects/codex/commerce_test/tmp/` | 임시 파일 보관소 (이동된 MEMORY_BANK, NEXT_AGENT_HANDOFF) |

---

## For AI Agents

### Working In This Directory

**프로젝트 루트에서 작업 시 필수 읽기:**

1. **설계 문서 우선 참조**: `doc/IMPLEMENTATION-PLAN.md` → `doc/ARCHITECTURE.md` → `doc/PRODUCT-BC.md` 순서로 읽는다. 설계 문서와 코드가 충돌하면 먼저 논의한다.

2. **Catalog→ProductGroup 리네이밍 규칙 준수**: `doc/IMPLEMENTATION-PLAN.md` 섹션 3에 매핑 테이블이 있다. Catalog 용어를 사용하지 않는다. 모든 클래스명, 테이블명, API 경로는 ProductGroup을 사용한다.

3. **도메인 레이어 순수성 보장**:
   - `domain/` 패키지는 순수 Java만 허용 (Spring, JPA, 외부 프레임워크 어노테이션 금지).
   - JPA Entity는 `infrastructure/persistence/entity/`에 별도 작성.
   - `infrastructure/mapper/`로 Domain ↔ Entity 변환.

4. **모듈 경계 준수**:
   - 모듈 간 참조는 `api/` 패키지의 인터페이스와 DTO를 통해서만 허용.
   - 다른 BC의 domain/infrastructure 패키지 직접 참조 금지.

5. **테스트 코드 필수 작성**:
   - 도메인 로직: 단위 테스트 (순수 Java)
   - Repository: @DataJpaTest + Testcontainers
   - Application Service: 통합 테스트
   - REST API: MockMvc 통합 테스트
   - ArchUnit: 아키텍처 규칙 검증 (`ArchitectureTest.java` 참조)

6. **JSONB/TEXT[] 컬럼 매핑**:
   - Hypersistence Utils 사용 (`io.hypersistence.utils.hibernate.type.json.JsonBinaryType`).
   - JPA Entity에 `@Type(JsonBinaryType.class)` 선언.

7. **빌드/실행 전 확인**:
   - Gradle 빌드: `./gradlew build` (루트에서 실행)
   - Docker Compose: `./scripts/compose-up.sh` (PostgreSQL init SQL 첫 실행 시만 적용됨)
   - DB 스키마 초기화: `./scripts/compose-down.sh --volumes` 후 재실행

8. **코드 외 모든 결과물은 한글**:
   - 커밋 메시지, 문서, 주석은 한글. 코드(클래스명, 메서드명, 변수명)는 영어.

### Testing Requirements

**테스트 전략 (Step 8 기준):**

| 테스트 유형 | 도구/프레임워크 | 목적 |
|------------|---------------|------|
| 도메인 단위 테스트 | JUnit 5 | 비즈니스 로직, 불변식 검증 (순수 Java, 프레임워크 없음) |
| Repository 테스트 | @DataJpaTest + Testcontainers (PostgreSQL) | JSONB, TEXT[], partial unique index, GIN index 매핑 검증 |
| Application Service 통합 테스트 | @SpringBootTest | 트랜잭션 경계, Repository 조합 검증 |
| REST API 테스트 | MockMvc + @WebMvcTest | Controller, DTO, Exception Handler 검증 |
| Stripe 동기화 테스트 | Mock Stripe API | 멱등성, 에러 처리 검증 |
| E2E 시나리오 테스트 | 인메모리 Repository + Stub Stripe | 전체 흐름 검증 (ProductScenarioTest) |
| 아키텍처 규칙 테스트 | ArchUnit | 7개 규칙 (도메인 순수성, 모듈 경계, 레이어 의존성) |
| 모듈 경계 테스트 | Spring Modulith | BC 간 순환 의존 검증 (ModulithStructureTest) |

**테스트 실행:**
```bash
# 전체 테스트 (108개)
./gradlew test

# 특정 모듈만
./gradlew :meditlink-commerce-core:test

# ArchUnit만
./gradlew :meditlink-commerce-core:test --tests ArchitectureTest
```

**테스트 작성 규칙:**
- Given-When-Then 패턴 사용 (BDD 스타일).
- 도메인 테스트는 `domain` 패키지와 동일한 구조로 `test/java/.../domain/` 하위에 작성.
- 통합 테스트는 `test/java/.../integration/` 하위에 작성.
- Testcontainers 사용 시 `@Testcontainers` + `@Container` 어노테이션.

### Common Patterns

**1. Aggregate 생성 패턴 (Factory Method):**
```java
// domain/productgroup/ProductGroup.java
public static ProductGroup create(ProductGroupId id, String name, Map<String, Object> attributes) {
    // 불변식 검증
    if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("ProductGroup name cannot be blank");
    }
    return new ProductGroup(id, name, ProductGroupStatus.DRAFT, attributes, LocalDateTime.now(), LocalDateTime.now());
}
```

**2. Domain ↔ Entity 매핑 패턴 (Mapper):**
```java
// infrastructure/mapper/ProductGroupMapper.java
public ProductGroupEntity toEntity(ProductGroup domain) {
    return new ProductGroupEntity(
        domain.id().value(),
        domain.name(),
        domain.status().name(),
        domain.attributes(),
        domain.createdAt(),
        domain.updatedAt()
    );
}

public ProductGroup toDomain(ProductGroupEntity entity) {
    return new ProductGroup(
        new ProductGroupId(entity.getId()),
        entity.getName(),
        ProductGroupStatus.valueOf(entity.getStatus()),
        entity.getAttributes(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
}
```

**3. JSONB 매핑 패턴 (JPA Entity):**
```java
@Entity
@Table(name = "product_groups", schema = "core")
public class ProductGroupEntity {
    @Type(JsonBinaryType.class)
    @Column(name = "attributes", columnDefinition = "jsonb")
    private Map<String, Object> attributes;
}
```

**4. Repository 패턴 (Port-Adapter):**
```java
// application/port/ProductGroupRepository.java (Port)
public interface ProductGroupRepository {
    ProductGroup save(ProductGroup productGroup);
    Optional<ProductGroup> findById(ProductGroupId id);
}

// infrastructure/persistence/adapter/ProductGroupRepositoryImpl.java (Adapter)
@Repository
public class ProductGroupRepositoryImpl implements ProductGroupRepository {
    private final ProductGroupJpaRepository jpaRepository;
    private final ProductGroupMapper mapper;

    @Override
    public ProductGroup save(ProductGroup productGroup) {
        ProductGroupEntity entity = mapper.toEntity(productGroup);
        ProductGroupEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
```

**5. Application Service 패턴 (트랜잭션 경계):**
```java
// application/command/ProductGroupCommandService.java
@Service
@Transactional
public class ProductGroupCommandService {
    private final ProductGroupRepository repository;

    public ProductGroup createProductGroup(CreateProductGroupCommand command) {
        ProductGroup productGroup = ProductGroup.create(
            new ProductGroupId(UUID.randomUUID()),
            command.name(),
            command.attributes()
        );
        return repository.save(productGroup);
    }
}
```

**6. Module API 패턴 (BC 간 통신):**
```java
// api/ProductModuleApi.java (인터페이스)
public interface ProductModuleApi {
    ProductGroupDto getProductGroup(UUID productGroupId);
}

// api/ProductModuleApiImpl.java (구현)
@Component
public class ProductModuleApiImpl implements ProductModuleApi {
    private final ProductQueryService queryService;

    @Override
    public ProductGroupDto getProductGroup(UUID productGroupId) {
        // ProductQueryService 호출 → DTO 변환
    }
}
```

**7. Rule Engine 사용 패턴 (가격 선택):**
```java
// shared/infra/rule/PriceSelector.java
public Price selectPrice(List<Price> prices, Map<String, Object> context) {
    return prices.stream()
        .filter(price -> {
            if (price.eligibilityRule() == null) return true;
            return ruleEngine.evaluate(price.eligibilityRule(), context);
        })
        .min(Comparator.comparing(Price::amount))
        .orElseThrow(() -> new IllegalStateException("No eligible price found"));
}
```

---

## Dependencies

**모듈 간 의존성:**
- `meditlink-commerce-core` → `meditlink-commerce-common-proto` (gRPC stub)
- `meditlink-commerce-client` → `meditlink-commerce-common-proto` (gRPC stub)
- `meditlink-commerce-client` → `meditlink-commerce-core` (Module API, gRPC 호출)

**외부 의존성 (libs.versions.toml):**
- **Spring Boot 4.0.0**: Web, Data JPA, Validation, Actuator
- **Spring Modulith 2.0.0**: 모듈 경계 검증
- **PostgreSQL 42.7.7**: RDBMS
- **Liquibase**: DB 스키마 버전 관리
- **Hypersistence Utils 3.15.2**: JSONB 매핑
- **Stripe Java SDK 28.3.0**: 결제 연동
- **gRPC 1.76.0**: 모듈 간 통신
- **ArchUnit 1.4.0**: 아키텍처 규칙 검증
- **Jackson 2.18.3**: Rule Engine JSON 역직렬화
- **Jib 3.4.5**: Dockerfile 없이 컨테이너 이미지 빌드

**DB 전략:**
- PostgreSQL 컨테이너 1개
- Database 1개: `meditlink_commerce`
- Schema 2개: `core`, `client`
- 로컬: docker init SQL로 최종 스키마/더미 데이터 로드 (`ddl-auto=validate`, `SPRING_LIQUIBASE_ENABLED=false`)
- 배포: Liquibase changeSet 기반 스키마 관리

**Init SQL 파일:**
1. `docker/postgres/init/01-init-schemas.sql` — core/client schema 생성
2. `docker/postgres/init/02-core-final-schema.sql` — core 테이블 (product_groups, products, product_features, prices)
3. `docker/postgres/init/03-client-final-schema.sql` — client 테이블 (integration_request_log)
4. `docker/postgres/init/04-core-dummy-data.sql` — 더미 데이터 (ProductGroup 2개, Product 4개, Price 8개)

**Jib Dockerizing:**
- core 이미지: `meditlink/commerce-core:local`
- client 이미지: `meditlink/commerce-client:local`
- 기본 아키텍처: `arm64` (Apple Silicon)
- Linux 배포용: `-PjibTargetArch=amd64` 오버라이드

**실행:**
```bash
# 원클릭 배포 (Jib 빌드 + Compose 실행)
./scripts/compose-up.sh

# 중지
./scripts/compose-down.sh

# 볼륨 삭제 (DB 초기화)
./scripts/compose-down.sh --volumes
```

---

**마지막 업데이트**: 2026-03-06 (Step 8 완료, 108개 테스트 통과)
