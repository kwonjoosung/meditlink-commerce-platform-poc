# product Bounded Context

<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-03-06 | Updated: 2026-03-06 -->

## Purpose

Product Bounded Context implementing the core product catalog domain with three aggregates: ProductGroup (catalog groupings), Product (billable items), and Price (pricing plans). Implements DDD layered architecture with domain, application, infrastructure, and api layers. Manages product lifecycle, conditional availability, and Stripe synchronization.

## Key Files

| File | Purpose |
|------|---------|
| `package-info.java` | Spring Modulith `@NamedInterface` defining public API surface |
| `domain/` | Pure domain models: ProductGroup, Product, ProductFeature, Price aggregates |
| `application/` | Application services: Command/Query services, DTOs, Port interfaces |
| `infrastructure/` | Technical implementation: JPA entities, repositories, Stripe sync, mappers |
| `api/` | Public interface for other modules: `ProductModuleApi`, DTOs |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `domain/` | Domain layer with ProductGroup, Product, Price aggregates (pure Java, no framework deps) |
| `application/` | Application layer: Command/Query services, DTOs, Port interfaces |
| `infrastructure/` | Infrastructure layer: JPA entities, repository adapters, Stripe sync, mappers |
| `api/` | Public API for inter-module communication: interface + DTOs |

## For AI Agents

### Working In This Directory

**Bounded Context Structure:**
- **Three Aggregates**: ProductGroup (groups products), Product (billable items), Price (pricing plans)
- **Aggregate Hierarchy**: ProductGroup → Product → Price (via foreign keys, not object references)
- **DDD Layers**: domain → application → infrastructure → api
- **Dependency Rule**: Layers depend inward only (infrastructure → application → domain)

**Aggregate Design:**
- **ProductGroup**: Catalog grouping with conditional visibility (Rule-based)
- **Product**: Billable item with ProductFeatures (value objects), belongs to ProductGroup
- **Price**: Pricing plan for Product with billing intervals, conditional selection

**Key Relationships:**
- ProductGroup ↔ Product: One-to-many via `productGroupId` foreign key
- Product ↔ ProductFeature: Composition (ProductFeature is value object within Product)
- Product ↔ Price: One-to-many via `productId` foreign key
- All use Rule-based conditions for dynamic behavior

**Status Management:**
- ProductGroup: DRAFT → ACTIVE → ARCHIVED
- Product: DRAFT → ACTIVE → ARCHIVED
- Business rules: Can only delete DRAFT entities, must have products before activating group (PoC relaxed)

**Stripe Integration:**
- `@Profile("stripe")`: Real Stripe SDK integration
- `@Profile("!stripe")`: Stub implementation (no-op)
- Synchronous: Create/update in domain triggers Stripe sync in application service

### Testing Requirements

**Domain Layer Tests (Pure Unit Tests):**
- Aggregate creation/reconstitution
- Business rule enforcement (status transitions, invariants)
- ProductFeature composition (add/remove/update)
- Rule validation integration

**Application Layer Tests:**
- Command service operations (create, update, delete)
- Query service data retrieval
- Transaction boundaries (`@Transactional`)
- Repository port integration

**Infrastructure Layer Tests:**
- JPA entity mapping (`@DataJpaTest` + Testcontainers)
- Repository adapter implementation
- Domain ↔ Entity mapping correctness
- JSONB/array column persistence
- Stripe sync service (mock Stripe SDK)

**API Layer Tests:**
- ProductModuleApi contract compliance
- DTO assembly correctness

### Common Patterns

**Domain Model Creation:**
```java
// Factory method for new aggregates
var productGroup = ProductGroup.create(name, slug, description);

// Reconstitute from persistence
var productGroup = ProductGroup.reconstitute(id, slug, name, ...);
```

**Application Service Pattern:**
```java
@Service
@Transactional
public class ProductCommandService {
    private final ProductRepository repository; // Port
    private final StripeProductSync stripeSync; // Port

    public Product create(CreateProductCommand cmd) {
        var product = Product.create(...);
        var saved = repository.save(product);
        stripeSync.syncToStripe(saved); // Stripe integration
        return saved;
    }
}
```

**Repository Port → Adapter:**
```java
// Port (application/port/)
public interface ProductRepository {
    Optional<Product> findById(ProductId id);
    Product save(Product product);
}

// Adapter (infrastructure/persistence/adapter/)
@Repository
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository jpaRepo;
    private final ProductMapper mapper;

    @Override
    public Product save(Product domain) {
        var entity = mapper.toEntity(domain);
        var saved = jpaRepo.save(entity);
        return mapper.toDomain(saved);
    }
}
```

**Mapper Pattern:**
```java
public class ProductMapper {
    public static ProductEntity toEntity(Product domain) {
        var entity = new ProductEntity();
        entity.setProductId(domain.getProductId().value());
        // ... map all fields, including JSONB
        return entity;
    }

    public static Product toDomain(ProductEntity entity) {
        return Product.reconstitute(
            ProductId.of(entity.getProductId()),
            // ... all fields
        );
    }
}
```

**Query Service Pattern:**
```java
@Service
@Transactional(readOnly = true)
public class ProductQueryService {
    public List<ProductGroup> findAllProductGroups() { ... }
    public Optional<Product> findProductById(String id) { ... }
    public List<Price> findPricesByProductId(String productId) { ... }
}
```

**Stripe Sync Pattern:**
```java
// Real implementation
@Service
@Profile("stripe")
public class StripeProductSyncService implements StripeProductSync {
    @Override
    public void syncToStripe(Product product) {
        // Call Stripe API
    }
}

// Stub implementation
@Service
@Profile("!stripe")
public class StubStripeProductSync implements StripeProductSync {
    @Override
    public void syncToStripe(Product product) {
        // No-op
    }
}
```

## Dependencies

### Internal

- `core.shared.domain`: ProductGroupId, ProductId, PriceId
- `core.shared.infra.rule`: Rule, RuleValidator, RuleContext, AttributeReader

### External

- Spring Framework: DI, transaction management
- Spring Data JPA: Persistence abstraction
- Stripe Java SDK: Payment processing
- Hypersistence Utils: JSONB mapping
- PostgreSQL JDBC: Database connectivity
