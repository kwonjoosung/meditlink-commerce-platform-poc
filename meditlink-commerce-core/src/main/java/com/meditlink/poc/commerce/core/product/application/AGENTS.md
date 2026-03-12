# product.application Layer

<!-- Parent: ../domain/AGENTS.md -->
<!-- Generated: 2026-03-06 | Updated: 2026-03-06 -->

## Purpose

Application layer implementing use cases via Command/Query services. Defines Port interfaces (repositories, external services) implemented by infrastructure. Contains internal DTOs (Commands) and orchestrates domain logic with transaction boundaries.

## Key Files

| File | Purpose |
|------|---------|
| `command/ProductGroupCommandService.java` | Command service: Create, update, delete, activate, archive ProductGroups |
| `command/ProductCommandService.java` | Command service: Create, update, delete Products; add/remove ProductFeatures |
| `command/PriceCommandService.java` | Command service: Create, delete Prices with Stripe sync |
| `command/package-info.java` | Package documentation |
| `query/ProductQueryService.java` | Query service: Read-only operations for all aggregates |
| `query/package-info.java` | Package documentation |
| `dto/CreateProductGroupCommand.java` | Command DTO: Create ProductGroup |
| `dto/UpdateProductGroupCommand.java` | Command DTO: Update ProductGroup |
| `dto/CreateProductCommand.java` | Command DTO: Create Product |
| `dto/UpdateProductCommand.java` | Command DTO: Update Product |
| `dto/CreatePriceCommand.java` | Command DTO: Create Price |
| `dto/AddFeatureCommand.java` | Command DTO: Add ProductFeature |
| `dto/package-info.java` | Package documentation |
| `port/ProductGroupRepository.java` | Port: ProductGroup persistence interface |
| `port/ProductRepository.java` | Port: Product persistence interface |
| `port/PriceRepository.java` | Port: Price persistence interface |
| `port/StripeProductSync.java` | Port: Stripe product synchronization interface |
| `port/StripePriceSync.java` | Port: Stripe price synchronization interface |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `command/` | Command services (write operations) with `@Transactional` |
| `query/` | Query services (read operations) with `@Transactional(readOnly = true)` |
| `dto/` | Internal DTOs (Command objects for service methods) |
| `port/` | Port interfaces (repositories, external services) implemented by infrastructure |

## For AI Agents

### Working In This Directory

**Application Layer Responsibilities:**
- **Use Case Orchestration**: Coordinate domain objects to fulfill business operations
- **Transaction Boundaries**: `@Transactional` on service methods
- **Port Definition**: Define interfaces for infrastructure (Dependency Inversion)
- **DTO Conversion**: Accept Commands, return domain objects (or DTOs if needed)
- **Error Handling**: Business exceptions propagate to gateway layer

**CQRS-lite Pattern:**
- **Command Services**: Write operations (create, update, delete) with `@Transactional`
- **Query Services**: Read operations with `@Transactional(readOnly = true)`
- Separation improves testability and allows future read/write optimization

**Port-Adapter Pattern:**
- Application defines **Ports** (interfaces): `ProductRepository`, `StripeProductSync`
- Infrastructure provides **Adapters** (implementations): `ProductRepositoryImpl`, `StripeProductSyncService`
- Dependency direction: Application → Port ← Adapter (dependency inversion)

**Command DTOs:**
- Simple records or classes carrying operation parameters
- Validation happens in domain layer (not DTOs)
- Immutable by convention

**Service Patterns:**
- Load aggregate → call domain method → save aggregate
- Transaction per service method (atomic operations)
- Stripe sync within same transaction (will be rolled back on failure)

### Testing Requirements

**Unit Tests (Mock Dependencies):**
- Mock repository ports and external service ports
- Verify domain method calls
- Verify repository.save() called
- Verify Stripe sync called when applicable

**Integration Tests (`@SpringBootTest`):**
- Real Spring context with test configuration
- Repository backed by Testcontainers PostgreSQL
- Stripe sync with stub implementation
- End-to-end command execution
- Query service data retrieval

**Test Patterns:**
```java
@ExtendWith(MockitoExtension.class)
class ProductGroupCommandServiceTest {
    @Mock ProductGroupRepository repository;
    @InjectMocks ProductGroupCommandService service;

    @Test
    void create_validCommand_savesAndReturns() {
        var cmd = new CreateProductGroupCommand("Basic", "basic", "Desc", null, null, null);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.create(cmd);

        assertThat(result.getName()).isEqualTo("Basic");
        verify(repository).save(any(ProductGroup.class));
    }
}
```

### Common Patterns

**Command Service:**
```java
@Service
@Transactional
public class ProductGroupCommandService {
    private final ProductGroupRepository repository;

    public ProductGroup create(CreateProductGroupCommand cmd) {
        var pg = ProductGroup.create(cmd.name(), cmd.slug(), cmd.description());
        if (cmd.attributes() != null) pg.updateAttributes(cmd.attributes());
        return repository.save(pg);
    }

    public ProductGroup update(String id, UpdateProductGroupCommand cmd) {
        var pg = repository.findById(ProductGroupId.of(id))
            .orElseThrow(() -> new IllegalArgumentException("Not found: " + id));
        pg.updateInfo(cmd.name(), cmd.slug(), cmd.description());
        return repository.save(pg);
    }
}
```

**Query Service:**
```java
@Service
@Transactional(readOnly = true)
public class ProductQueryService {
    private final ProductGroupRepository productGroupRepository;
    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;

    public List<ProductGroup> findAllProductGroups() {
        return productGroupRepository.findAll();
    }

    public Optional<Product> findProductById(String id) {
        return productRepository.findById(ProductId.of(id));
    }

    public List<Price> findPricesByProductId(String productId) {
        return priceRepository.findByProductId(ProductId.of(productId));
    }
}
```

**Port Interface:**
```java
public interface ProductGroupRepository {
    Optional<ProductGroup> findById(ProductGroupId id);
    List<ProductGroup> findAll();
    ProductGroup save(ProductGroup productGroup);
    void deleteById(ProductGroupId id);
}
```

**Stripe Integration in Command Service:**
```java
@Service
@Transactional
public class ProductCommandService {
    private final ProductRepository repository;
    private final StripeProductSync stripeSync; // Port

    public Product create(CreateProductCommand cmd) {
        var product = Product.create(...);
        var saved = repository.save(product);
        stripeSync.syncToStripe(saved); // External service call within transaction
        return saved;
    }
}
```

**Command DTO:**
```java
public record CreateProductGroupCommand(
    String name,
    String slug,
    String description,
    Map<String, Object> attributes,
    Map<String, Object> metadata,
    List<String> tags
) {}
```

## Dependencies

### Internal

- `core.product.domain`: ProductGroup, Product, Price aggregates
- `core.shared.domain`: ProductGroupId, ProductId, PriceId
- `core.shared.infra.rule`: Rule (for condition fields in Commands)

### External

- Spring Framework: `@Service`, `@Transactional`
- Spring Context: Dependency injection
