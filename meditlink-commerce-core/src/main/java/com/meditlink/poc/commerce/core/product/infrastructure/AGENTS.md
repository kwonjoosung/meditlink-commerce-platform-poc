# product.infrastructure Layer

<!-- Parent: ../application/AGENTS.md -->
<!-- Generated: 2026-03-06 | Updated: 2026-03-06 -->

## Purpose

Infrastructure layer providing technical implementations: JPA entities with JSONB/array mappings, repository adapters implementing domain ports, domain↔entity mappers, and Stripe synchronization services (real + stub). Handles persistence concerns separate from domain logic.

## Key Files

| File | Purpose |
|------|---------|
| `persistence/entity/ProductGroupEntity.java` | JPA Entity: ProductGroup with JSONB columns (condition, attributes, metadata, tags[]) |
| `persistence/entity/ProductEntity.java` | JPA Entity: Product with JSONB columns |
| `persistence/entity/ProductFeatureEntity.java` | JPA Entity: ProductFeature (owned by Product) |
| `persistence/entity/PriceEntity.java` | JPA Entity: Price with JSONB columns |
| `persistence/repository/ProductGroupJpaRepository.java` | Spring Data JPA repository interface for ProductGroupEntity |
| `persistence/repository/ProductJpaRepository.java` | Spring Data JPA repository interface for ProductEntity |
| `persistence/repository/ProductFeatureJpaRepository.java` | Spring Data JPA repository interface for ProductFeatureEntity |
| `persistence/repository/PriceJpaRepository.java` | Spring Data JPA repository interface for PriceEntity |
| `persistence/adapter/ProductGroupRepositoryImpl.java` | Adapter: Implements ProductGroupRepository port using JPA + Mapper |
| `persistence/adapter/ProductRepositoryImpl.java` | Adapter: Implements ProductRepository port using JPA + Mapper |
| `persistence/adapter/PriceRepositoryImpl.java` | Adapter: Implements PriceRepository port using JPA + Mapper |
| `mapper/ProductGroupMapper.java` | Mapper: Domain ↔ Entity conversion for ProductGroup |
| `mapper/ProductMapper.java` | Mapper: Domain ↔ Entity conversion for Product (with ProductFeature mapping) |
| `mapper/PriceMapper.java` | Mapper: Domain ↔ Entity conversion for Price |
| `stripe/StripeConfig.java` | Configuration: Stripe API key setup |
| `stripe/StripeProductSyncService.java` | Real implementation: Syncs Product to Stripe (`@Profile("stripe")`) |
| `stripe/StripePriceSyncService.java` | Real implementation: Syncs Price to Stripe (`@Profile("stripe")`) |
| `stripe/StubStripeProductSync.java` | Stub implementation: No-op for testing (`@Profile("!stripe")`) |
| `stripe/StubStripePriceSync.java` | Stub implementation: No-op for testing (`@Profile("!stripe")`) |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `persistence/entity/` | JPA entities with Hibernate/JPA annotations |
| `persistence/repository/` | Spring Data JPA repository interfaces |
| `persistence/adapter/` | Repository adapters implementing domain ports |
| `mapper/` | Domain ↔ Entity bidirectional mapping |
| `stripe/` | Stripe SDK integration (real + stub implementations) |

## For AI Agents

### Working In This Directory

**Domain-Entity Separation:**
- **Domain Models**: Pure Java in `domain/` package (no JPA)
- **JPA Entities**: Annotated classes in `infrastructure/persistence/entity/`
- **Mappers**: Convert between domain ↔ entity in both directions
- **Reason**: Domain layer stays independent of persistence technology

**JPA Entity Patterns:**
- `@Entity`, `@Table`, `@Id`, `@Column` annotations
- JSONB columns: `@Type(JsonBinaryType.class)` from Hypersistence Utils
- Array columns: `String[]` mapped to PostgreSQL `text[]`
- Foreign keys: UUID fields (productGroupId, productId)
- No bidirectional relationships (unidirectional FK only)

**Repository Adapter Pattern:**
```
Application defines Port → Infrastructure implements Adapter
ProductRepository (port) ← ProductRepositoryImpl (adapter) uses ProductJpaRepository + ProductMapper
```

**Stripe Integration:**
- Two profiles: `stripe` (real) and `!stripe` (stub)
- Real implementation calls Stripe API SDK
- Stub implementation is no-op (logs only)
- Spring profile controls which bean is loaded

**Mapper Responsibilities:**
- `toDomain()`: JPA Entity → Domain Model (for reads)
- `toEntity()`: Domain Model → JPA Entity (for writes)
- Rule serialization: Rule → JSON (stored in JSONB)
- Rule deserialization: JSON → Rule (loaded from JSONB)
- Collection mapping: List<ProductFeature> ↔ List<ProductFeatureEntity>

### Testing Requirements

**Repository Tests (`@DataJpaTest`):**
- Testcontainers PostgreSQL
- JPA entity CRUD operations
- JSONB column persistence/retrieval
- Array column persistence/retrieval
- Foreign key constraints
- Query methods

**Adapter Tests:**
- Mock JPA repository and mapper
- Verify domain↔entity conversion flow
- Verify adapter delegates to JPA repository correctly

**Mapper Tests:**
- Domain → Entity → Domain roundtrip
- Null/empty collection handling
- JSONB serialization/deserialization
- ProductFeature list mapping

**Stripe Sync Tests:**
- Mock Stripe SDK
- Verify API calls with correct parameters
- Error handling (Stripe API failures)
- Stub implementation (verify no-op)

### Common Patterns

**JPA Entity:**
```java
@Entity
@Table(name = "product_groups")
public class ProductGroupEntity {
    @Id
    @Column(name = "product_group_id")
    private UUID productGroupId;

    @Column(name = "name", nullable = false)
    private String name;

    @Type(JsonBinaryType.class)
    @Column(name = "condition", columnDefinition = "jsonb")
    private String conditionJson; // Rule serialized

    @Type(JsonBinaryType.class)
    @Column(name = "attributes", columnDefinition = "jsonb")
    private Map<String, Object> attributes;

    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;

    // Getters/setters
}
```

**Spring Data JPA Repository:**
```java
public interface ProductGroupJpaRepository extends JpaRepository<ProductGroupEntity, UUID> {
    // Spring Data generates implementation
    List<ProductGroupEntity> findByStatus(String status);
}
```

**Repository Adapter:**
```java
@Repository
public class ProductGroupRepositoryImpl implements ProductGroupRepository {
    private final ProductGroupJpaRepository jpaRepo;
    private final ProductGroupMapper mapper;

    @Override
    public Optional<ProductGroup> findById(ProductGroupId id) {
        return jpaRepo.findById(id.value())
            .map(mapper::toDomain);
    }

    @Override
    public ProductGroup save(ProductGroup domain) {
        var entity = mapper.toEntity(domain);
        var saved = jpaRepo.save(entity);
        return mapper.toDomain(saved);
    }
}
```

**Mapper:**
```java
public class ProductGroupMapper {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public static ProductGroupEntity toEntity(ProductGroup domain) {
        var entity = new ProductGroupEntity();
        entity.setProductGroupId(domain.getProductGroupId().value());
        entity.setName(domain.getName());

        // Serialize Rule to JSON
        if (domain.getCondition() != null) {
            try {
                entity.setConditionJson(JSON_MAPPER.writeValueAsString(domain.getCondition()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize condition", e);
            }
        }

        entity.setAttributes(domain.getAttributes());
        entity.setTags(domain.getTags().toArray(new String[0]));
        return entity;
    }

    public static ProductGroup toDomain(ProductGroupEntity entity) {
        Rule condition = null;
        if (entity.getConditionJson() != null) {
            try {
                condition = JSON_MAPPER.readValue(entity.getConditionJson(), Rule.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize condition", e);
            }
        }

        return ProductGroup.reconstitute(
            ProductGroupId.of(entity.getProductGroupId()),
            entity.getSlug(),
            entity.getName(),
            // ... all fields
            condition,
            entity.getAttributes(),
            // ...
            Arrays.asList(entity.getTags() != null ? entity.getTags() : new String[0]),
            // ...
        );
    }
}
```

**Stripe Sync (Real):**
```java
@Service
@Profile("stripe")
public class StripeProductSyncService implements StripeProductSync {
    @Override
    public void syncToStripe(Product product) {
        var params = ProductCreateParams.builder()
            .setName(product.getName())
            .setDescription(product.getDescription())
            // ... map fields
            .build();

        try {
            com.stripe.model.Product.create(params);
        } catch (StripeException e) {
            throw new RuntimeException("Stripe sync failed", e);
        }
    }
}
```

**Stripe Sync (Stub):**
```java
@Service
@Profile("!stripe")
public class StubStripeProductSync implements StripeProductSync {
    private static final Logger log = LoggerFactory.getLogger(StubStripeProductSync.class);

    @Override
    public void syncToStripe(Product product) {
        log.info("Stub: Would sync product {} to Stripe", product.getProductId());
        // No-op
    }
}
```

## Dependencies

### Internal

- `core.product.domain`: Domain models (ProductGroup, Product, Price)
- `core.product.application.port`: Port interfaces to implement
- `core.shared.domain`: Value Objects (ProductGroupId, etc.)
- `core.shared.infra.rule`: Rule for serialization

### External

- Spring Data JPA: Repository abstraction, entity management
- Hibernate: JPA implementation
- PostgreSQL JDBC Driver: Database connectivity
- Hypersistence Utils: `@Type(JsonBinaryType.class)` for JSONB
- Stripe Java SDK: `com.stripe.model.*`, `com.stripe.param.*`
- Jackson: JSON serialization for Rule
