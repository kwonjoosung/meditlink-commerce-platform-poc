# product.domain Layer

<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-03-06 | Updated: 2026-03-06 -->

## Purpose

Pure domain layer implementing three aggregates: ProductGroup, Product (with ProductFeature value objects), and Price. Contains business logic, invariants, and state transitions without any framework dependencies. Uses Rule Engine for conditional logic.

## Key Files

| File | Purpose |
|------|---------|
| `productgroup/ProductGroup.java` | Aggregate Root: Catalog grouping with conditional visibility |
| `productgroup/ProductGroupStatus.java` | Enum: DRAFT, ACTIVE, ARCHIVED status values |
| `productgroup/package-info.java` | Package documentation |
| `product/Product.java` | Aggregate Root: Billable item with features, belongs to ProductGroup |
| `product/ProductFeature.java` | Value Object: Feature definition within Product |
| `product/ProductStatus.java` | Enum: DRAFT, ACTIVE, ARCHIVED status values |
| `product/package-info.java` | Package documentation |
| `price/Price.java` | Aggregate Root: Pricing plan for Product with billing intervals |
| `price/package-info.java` | Package documentation |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `productgroup/` | ProductGroup aggregate with status management |
| `product/` | Product aggregate with ProductFeature composition |
| `price/` | Price aggregate with billing interval logic |

## For AI Agents

### Working In This Directory

**Domain Purity Rules (CRITICAL):**
- **NO Spring dependencies**: No `@Component`, `@Service`, `@Entity`, `@Transactional`
- **NO JPA annotations**: No `@Entity`, `@Table`, `@Column`, `@Id`
- **NO infrastructure dependencies**: No database, HTTP, external service calls
- **Pure Java only**: Records, enums, classes, interfaces, java.time, java.util

**Aggregate Patterns:**
- **Factory Methods**: `create()` for new instances, `reconstitute()` for persistence restoration
- **Private Constructor**: Enforce factory method usage
- **Encapsulation**: Private setters, public behavioral methods
- **Immutability**: Value Objects (ProductFeature) are immutable
- **Self-Validation**: Throw `IllegalArgumentException` for business rule violations

**ProductGroup Aggregate:**
- **Identity**: ProductGroupId (UUID wrapper)
- **Value Objects**: slug, name, description, status, displayOrder, condition (Rule), attributes, metadata, tags
- **Business Methods**: activate(), archive(), reactivate(), updateInfo(), updateCondition(), updateDisplayOrder()
- **Invariants**: name cannot be blank, condition must be valid Rule, ARCHIVED → only reactivate()
- **Rule Integration**: Uses RuleValidator to validate conditions

**Product Aggregate:**
- **Identity**: ProductId (UUID wrapper)
- **Foreign Key**: productGroupId (references ProductGroup)
- **Composition**: List<ProductFeature> features (value objects managed by Product)
- **Value Objects**: name, description, type, billingType, status, condition (Rule), attributes, metadata, tags
- **Business Methods**: activate(), archive(), addFeature(), removeFeature(), updateFeature()
- **Invariants**: name cannot be blank, features have unique codes, billingType determines pricing rules

**ProductFeature Value Object:**
- **Composition**: Managed by Product aggregate (not separate entity)
- **Fields**: code (unique within Product), name, description, featureType, isEnabled
- **Immutability**: No setters, replaced entirely when updated
- **Factory**: `ProductFeature.of(code, name, description, featureType, isEnabled)`

**Price Aggregate:**
- **Identity**: PriceId (UUID wrapper)
- **Foreign Key**: productId (references Product)
- **Value Objects**: currency, amount, billingInterval, intervalCount, isDefault, condition (Rule), attributes, metadata, tags
- **Business Methods**: setDefault(), updateCondition()
- **Invariants**: Only one default price per Product (enforced at application layer), amount > 0, billingInterval compatibility

**Status Lifecycle:**
```
DRAFT → ACTIVE → ARCHIVED
         ↑          ↓
         └── reactivate() ──┘
```

**Rule Integration:**
- Aggregates store `Rule condition` for conditional behavior
- `RuleValidator.validate(rule)` ensures rule correctness before storing
- Evaluation happens in application/infrastructure layers (not domain)

### Testing Requirements

**Pure Unit Tests (No Spring Context):**
- Aggregate creation via factory methods
- Status transitions (activate, archive, reactivate)
- Business rule enforcement (invalid state transitions throw exceptions)
- ProductFeature composition (add, remove, update)
- Rule validation integration
- Edge cases: null handling, empty strings, duplicate features

**Test Patterns:**
```java
@Test
void createProductGroup_validData_success() {
    var pg = ProductGroup.create("Basic Plan", "basic", "Entry level");
    assertThat(pg.getProductGroupId()).isNotNull();
    assertThat(pg.getStatus()).isEqualTo(ProductGroupStatus.DRAFT);
}

@Test
void activate_fromDraft_success() {
    var pg = ProductGroup.create("Basic", "basic", "Desc");
    pg.activate();
    assertThat(pg.getStatus()).isEqualTo(ProductGroupStatus.ACTIVE);
}

@Test
void reactivate_notArchived_throwsException() {
    var pg = ProductGroup.create("Basic", "basic", "Desc");
    pg.activate();
    assertThatThrownBy(() -> pg.reactivate())
        .isInstanceOf(IllegalStateException.class);
}
```

### Common Patterns

**Factory Method (create):**
```java
public static ProductGroup create(String name, String slug, String description) {
    if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("name은 비어 있을 수 없습니다");
    }
    var pg = new ProductGroup();
    pg.productGroupId = ProductGroupId.generate();
    pg.slug = slug;
    pg.name = name;
    pg.description = description;
    pg.status = ProductGroupStatus.DRAFT;
    pg.createdAt = Instant.now();
    pg.updatedAt = Instant.now();
    return pg;
}
```

**Factory Method (reconstitute):**
```java
public static ProductGroup reconstitute(
        ProductGroupId id, String slug, String name, ...) {
    var pg = new ProductGroup();
    pg.productGroupId = id;
    pg.slug = slug;
    // ... set all fields from persistence
    return pg;
}
```

**Business Method:**
```java
public void activate() {
    this.status = ProductGroupStatus.ACTIVE;
    this.updatedAt = Instant.now();
}

public void updateCondition(Rule condition) {
    if (condition != null) {
        var result = RuleValidator.validate(condition);
        if (!result.valid()) {
            throw new IllegalArgumentException("유효하지 않은 조건: " + result.errors());
        }
    }
    this.condition = condition;
    this.updatedAt = Instant.now();
}
```

**Value Object Composition (ProductFeature):**
```java
// Add feature
public void addFeature(ProductFeature feature) {
    if (features.stream().anyMatch(f -> f.getCode().equals(feature.getCode()))) {
        throw new IllegalArgumentException("중복된 feature code: " + feature.getCode());
    }
    this.features = new ArrayList<>(features);
    this.features.add(feature);
    this.updatedAt = Instant.now();
}

// Remove feature
public void removeFeature(String featureCode) {
    this.features = features.stream()
        .filter(f -> !f.getCode().equals(featureCode))
        .toList();
    this.updatedAt = Instant.now();
}
```

**Immutable Value Object:**
```java
public record ProductFeature(
    String code,
    String name,
    String description,
    String featureType,
    boolean isEnabled
) {
    public ProductFeature {
        Objects.requireNonNull(code, "code는 null일 수 없습니다");
        Objects.requireNonNull(name, "name은 null일 수 없습니다");
    }

    public static ProductFeature of(String code, String name, String description,
                                     String featureType, boolean isEnabled) {
        return new ProductFeature(code, name, description, featureType, isEnabled);
    }
}
```

## Dependencies

### Internal

- `core.shared.domain`: ProductGroupId, ProductId, PriceId
- `core.shared.infra.rule`: Rule, RuleValidator (infrastructure used by domain for validation)

### External

- Java 21: Records, sealed types
- java.time: Instant for timestamps
- java.util: Collections, Map, List, Objects
