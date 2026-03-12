# meditlink-commerce-core Module

<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-03-06 | Updated: 2026-03-06 -->

## Purpose

Core business logic module of the E-Commerce PoC implementing DDD modular monolith architecture. Contains Product Bounded Context (ProductGroup, Product, ProductFeature, Price aggregates) with shared infrastructure (Rule Engine, Attributes). Manages product catalog and pricing with conditional logic and Stripe synchronization.

## Key Files

| File | Purpose |
|------|---------|
| `CoreServiceApplication.java` | Spring Boot main application class |
| `src/main/java/com/meditlink/poc/commerce/core/shared/` | Shared module: Value Objects, Rule Engine, Attributes |
| `src/main/java/com/meditlink/poc/commerce/core/product/` | Product BC: ProductGroup, Product, Price aggregates with DDD layers |
| `src/main/java/com/meditlink/poc/commerce/core/gateway/` | REST API controllers and exception handling |
| `src/main/java/com/meditlink/poc/commerce/core/coupon/` | Coupon BC (existing, PoC scope external) |
| `src/main/java/com/meditlink/poc/commerce/core/config/` | Configuration classes (gRPC lifecycle) |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `shared/` | Shared Value Objects (ProductGroupId, ProductId, PriceId), Rule Engine, Attribute system |
| `product/` | Product Bounded Context with domain, application, infrastructure, api layers |
| `gateway/` | REST controllers (`ProductController`), global exception handler |
| `coupon/` | Coupon Bounded Context (existing, maintained for reference) |
| `config/` | Spring configuration classes |

## For AI Agents

### Working In This Directory

**Architecture:**
- **DDD Modular Monolith**: Each Bounded Context (BC) is a separate module package
- **Layered Architecture**: domain → application → infrastructure → api
- **Spring Modulith**: Uses `@NamedInterface` for explicit module boundaries
- **Port-Adapter Pattern**: Application layer defines Ports (interfaces), Infrastructure provides Adapters

**Core Principles:**
1. **Domain Purity**: Domain layer must NOT depend on Spring, JPA, or infrastructure
2. **Dependency Inversion**: Infrastructure implements domain-defined interfaces (Ports)
3. **Aggregate Isolation**: Each Aggregate Root manages its own boundaries
4. **Domain-Entity Separation**: Pure domain models separate from JPA entities with explicit mappers

**Module Structure:**
- `shared/`: Common Value Objects, Rule Engine, Attribute system used across BCs
- `product/`: Primary BC implementing ProductGroup-Product-Price hierarchy
- `gateway/`: BFF layer exposing REST APIs (not part of domain)
- `coupon/`: Existing BC (PoC scope external, maintained for modulith demonstration)

**Key Design Patterns:**
- **Rule Engine**: Composable condition evaluation (CompositeRule, LeafRule) for ProductGroup/Product/Price
- **Price Selector**: Evaluates Rule conditions against RuleContext to select applicable Prices
- **Attribute System**: Typed enums (ProductGroupAttribute, ProductAttribute, PriceAttribute) with AttributeReader
- **Domain Events**: (Future) Spring ApplicationEvents for inter-module communication
- **CQRS-lite**: Separate Command/Query services in application layer

### Testing Requirements

**Unit Tests:**
- Domain models: Pure business logic tests (no Spring context)
- Rule Engine: Condition evaluation, validation, price selection
- Attribute System: Attribute reading, validation

**Integration Tests:**
- Repository tests: `@DataJpaTest` with Testcontainers PostgreSQL
- Service tests: `@SpringBootTest` for application service integration
- Controller tests: `@WebMvcTest` with MockMvc

**Architecture Tests:**
- ArchUnit tests enforce: domain independence, layer boundaries, module isolation
- Spring Modulith structure tests validate module boundaries

**Required Test Coverage:**
- Domain layer: >90% (business logic critical)
- Application layer: >80%
- Infrastructure layer: >70%

### Common Patterns

**Domain Model Creation:**
```java
// Factory method pattern
public static ProductGroup create(String name, String slug, String description) {
    var pg = new ProductGroup();
    pg.productGroupId = ProductGroupId.generate();
    // ... initialize fields
    return pg;
}

// Reconstitute from persistence
public static ProductGroup reconstitute(ProductGroupId id, ...) {
    var pg = new ProductGroup();
    // ... restore all fields
    return pg;
}
```

**Application Service Pattern:**
```java
@Service
@Transactional
public class ProductGroupCommandService {
    private final ProductGroupRepository repository; // Port interface

    public ProductGroup create(CreateProductGroupCommand cmd) {
        var pg = ProductGroup.create(...);
        return repository.save(pg);
    }
}
```

**Domain ↔ Entity Mapping:**
```java
// Infrastructure mapper
public class ProductGroupMapper {
    public static ProductGroupEntity toEntity(ProductGroup domain) { ... }
    public static ProductGroup toDomain(ProductGroupEntity entity) { ... }
}
```

**Rule Engine Usage:**
```java
// Validate rule
var result = RuleValidator.validate(rule);
if (!result.valid()) throw new IllegalArgumentException(...);

// Evaluate rule
var context = new RuleContext(attributes);
boolean matches = rule.evaluate(context);

// Select price
List<Price> applicable = PriceSelector.selectApplicable(prices, context);
```

**Stripe Synchronization:**
```java
@Profile("stripe")  // Real implementation
@Profile("!stripe") // Stub implementation
```

## Dependencies

### Internal

- `shared.domain`: Value Objects (ProductGroupId, ProductId, PriceId)
- `shared.infra.rule`: Rule Engine for conditional logic
- `product.api`: Public interfaces and DTOs for inter-module communication

### External

- Spring Boot 3.x: DI, transaction management, web
- Spring Data JPA: Persistence layer
- Spring Modulith: Module boundaries and events
- PostgreSQL: Database with JSONB support
- Hypersistence Utils: JSONB mapping
- Stripe Java SDK: Payment processing sync
- ArchUnit: Architecture testing
- Testcontainers: Integration testing
