# gateway Layer

<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-03-06 | Updated: 2026-03-06 -->

## Purpose

Gateway layer providing REST API endpoints for Product BC operations. Acts as BFF (Backend for Frontend) exposing ProductGroup, Product, ProductFeature, and Price CRUD operations. Includes global exception handling for consistent error responses.

## Key Files

| File | Purpose |
|------|---------|
| `ProductController.java` | REST controller: Complete CRUD for ProductGroup, Product, ProductFeature, Price |
| `GlobalExceptionHandler.java` | Exception handler: Converts domain exceptions to HTTP responses |
| `package-info.java` | Package documentation |

## Subdirectories

None (flat structure)

## For AI Agents

### Working In This Directory

**Gateway Responsibilities:**
- **REST API Exposure**: HTTP endpoints for all Product BC operations
- **DTO Assembly**: Aggregate data from multiple queries into nested DTOs
- **HTTP Mapping**: Translate domain operations to RESTful conventions
- **Exception Translation**: Domain exceptions → HTTP status codes + error messages

**Controller Design:**
- Single controller (`ProductController`) for entire Product BC
- Hierarchical endpoints: `/api/product-groups/{id}/products/{id}/...`
- Delegates to application services (Command/Query services)
- Uses `DtoAssembler` to convert domain → API DTOs

**REST API Conventions:**
- POST: Create resources (201 Created)
- GET: Retrieve resources (200 OK, 404 Not Found)
- PUT: Update resources (200 OK)
- DELETE: Delete resources (204 No Content)
- POST actions: `/api/product-groups/{id}/activate` (200 OK)

**DTO Assembly Pattern:**
- Query service returns domain models
- Controller assembles nested DTOs (ProductGroup → Products → Prices)
- `DtoAssembler` handles domain → DTO conversion
- Nested queries in loops (N+1 acceptable for PoC)

**Error Handling:**
- `@RestControllerAdvice` global exception handler
- `IllegalArgumentException` → 400 Bad Request
- `IllegalStateException` → 409 Conflict
- `NoSuchElementException` → 404 Not Found
- Generic `Exception` → 500 Internal Server Error

### Testing Requirements

**Controller Tests (`@WebMvcTest`):**
- MockMvc for HTTP endpoint testing
- Mock application services
- Verify HTTP status codes
- Verify JSON response structure
- Verify error responses

**Integration Tests (`@SpringBootTest`):**
- Full Spring context with Testcontainers
- End-to-end HTTP → service → database flow
- REST Assured or TestRestTemplate
- Verify transaction rollback on errors

**Test Patterns:**
```java
@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean ProductGroupCommandService commandService;
    @MockBean ProductQueryService queryService;

    @Test
    void createProductGroup_validRequest_returns201() throws Exception {
        var pg = ProductGroup.create("Basic", "basic", "Desc");
        when(commandService.create(any())).thenReturn(pg);

        mockMvc.perform(post("/api/product-groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Basic\",\"slug\":\"basic\",\"description\":\"Desc\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Basic"));
    }
}
```

### Common Patterns

**Controller Endpoint:**
```java
@RestController
@RequestMapping("/api")
public class ProductController {
    private final ProductGroupCommandService productGroupCommandService;
    private final ProductQueryService queryService;

    @PostMapping("/product-groups")
    public ResponseEntity<ProductGroupDto> createProductGroup(@RequestBody CreateProductGroupCommand cmd) {
        var pg = productGroupCommandService.create(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoAssembler.toDto(pg));
    }

    @GetMapping("/product-groups/{id}")
    public ResponseEntity<ProductGroupDto> getProductGroup(@PathVariable String id) {
        return queryService.findProductGroupById(id)
            .map(pg -> {
                var products = queryService.findProductsByProductGroupId(id);
                var productDtos = products.stream().map(p -> {
                    var prices = queryService.findPricesByProductId(p.getProductId().toString());
                    return DtoAssembler.toDto(p, prices.stream().map(DtoAssembler::toDto).toList());
                }).toList();
                return ResponseEntity.ok(DtoAssembler.toDto(pg, productDtos));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/product-groups/{id}")
    public ResponseEntity<ProductGroupDto> updateProductGroup(
            @PathVariable String id,
            @RequestBody UpdateProductGroupCommand cmd) {
        var pg = productGroupCommandService.update(id, cmd);
        return ResponseEntity.ok(DtoAssembler.toDto(pg));
    }

    @DeleteMapping("/product-groups/{id}")
    public ResponseEntity<Void> deleteProductGroup(@PathVariable String id) {
        productGroupCommandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Action Endpoint:**
```java
@PostMapping("/product-groups/{id}/activate")
public ResponseEntity<ProductGroupDto> activateProductGroup(@PathVariable String id) {
    var pg = productGroupCommandService.activate(id);
    return ResponseEntity.ok(DtoAssembler.toDto(pg));
}

@PostMapping("/product-groups/{id}/archive")
public ResponseEntity<ProductGroupDto> archiveProductGroup(@PathVariable String id) {
    var pg = productGroupCommandService.archive(id);
    return ResponseEntity.ok(DtoAssembler.toDto(pg));
}
```

**Nested Resource:**
```java
@PostMapping("/product-groups/{productGroupId}/products")
public ResponseEntity<ProductDto> createProduct(
        @PathVariable String productGroupId,
        @RequestBody CreateProductCommand cmd) {
    var fullCmd = new CreateProductCommand(
        productGroupId,
        cmd.name(),
        cmd.description(),
        // ... merge path param with body
    );
    var product = productCommandService.create(fullCmd);
    return ResponseEntity.status(HttpStatus.CREATED).body(DtoAssembler.toDto(product));
}

@PostMapping("/products/{productId}/features")
public ResponseEntity<ProductDto> addFeature(
        @PathVariable String productId,
        @RequestBody AddFeatureCommand cmd) {
    var product = productCommandService.addFeature(productId, cmd);
    return ResponseEntity.status(HttpStatus.CREATED).body(DtoAssembler.toDto(product));
}
```

**Global Exception Handler:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_INPUT", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse("INVALID_STATE", e.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    record ErrorResponse(String code, String message) {}
}
```

**DTO Assembly:**
```java
// In api/dto/DtoAssembler.java
public class DtoAssembler {
    public static ProductGroupDto toDto(ProductGroup pg) {
        return new ProductGroupDto(
            pg.getProductGroupId().toString(),
            pg.getName(),
            pg.getSlug(),
            pg.getDescription(),
            pg.getStatus().name(),
            List.of() // No products in simple DTO
        );
    }

    public static ProductGroupDto toDto(ProductGroup pg, List<ProductDto> products) {
        return new ProductGroupDto(
            pg.getProductGroupId().toString(),
            pg.getName(),
            pg.getSlug(),
            pg.getDescription(),
            pg.getStatus().name(),
            products
        );
    }

    public static ProductDto toDto(Product p, List<PriceDto> prices) {
        return new ProductDto(
            p.getProductId().toString(),
            p.getName(),
            p.getDescription(),
            p.getType(),
            p.getStatus().name(),
            p.getFeatures().stream().map(DtoAssembler::toDto).toList(),
            prices
        );
    }
}
```

## Dependencies

### Internal

- `core.product.application.command`: Command services
- `core.product.application.query`: Query services
- `core.product.application.dto`: Command DTOs
- `core.product.api.dto`: API DTOs and DtoAssembler

### External

- Spring Web: `@RestController`, `@RequestMapping`, `@GetMapping`, etc.
- Spring Web: `ResponseEntity`, HTTP status codes
- Spring Web: `@RestControllerAdvice`, `@ExceptionHandler`
- Jackson: Automatic JSON serialization/deserialization
