# shared Module

<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-03-06 | Updated: 2026-03-06 -->

## Purpose

Shared infrastructure module providing common Value Objects, Rule Engine for conditional logic evaluation, and Attribute system for typed metadata access. Used across all Bounded Contexts without coupling to specific domain logic.

## Key Files

| File | Purpose |
|------|---------|
| `domain/ProductGroupId.java` | Value Object: UUID wrapper for ProductGroup identity |
| `domain/ProductId.java` | Value Object: UUID wrapper for Product identity |
| `domain/PriceId.java` | Value Object: UUID wrapper for Price identity |
| `domain/package-info.java` | Package documentation for shared domain types |
| `infra/rule/Rule.java` | Sealed interface: Base for composable condition rules |
| `infra/rule/CompositeRule.java` | Composite pattern: AND/OR rule composition |
| `infra/rule/LeafRule.java` | Leaf rule: Single condition evaluation (attribute operator value) |
| `infra/rule/RuleOperator.java` | Enum: Supported operators (EQUALS, GREATER_THAN, etc.) |
| `infra/rule/RuleOperators.java` | Factory: Create operator implementations |
| `infra/rule/RuleContext.java` | Context: Attribute map for rule evaluation |
| `infra/rule/RuleEngine.java` | Engine: Orchestrates rule evaluation |
| `infra/rule/RuleValidator.java` | Validator: Validates rule structure and semantics |
| `infra/rule/RuleDeserializer.java` | Deserializer: Custom Jackson deserializer for Rule polymorphism |
| `infra/rule/PriceSelector.java` | Selector: Finds applicable Prices using Rule evaluation |
| `infra/rule/AttributeDefinition.java` | Metadata: Defines valid attributes per entity type |
| `infra/rule/AttributeReader.java` | Reader: Extracts attribute values from domain objects |
| `infra/rule/ProductGroupAttribute.java` | Enum: Valid attributes for ProductGroup |
| `infra/rule/ProductAttribute.java` | Enum: Valid attributes for Product |
| `infra/rule/PriceAttribute.java` | Enum: Valid attributes for Price |
| `infra/rule/package-info.java` | Package documentation for rule engine |
| `package-info.java` | Module-level package documentation |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `domain/` | Common Value Objects used across BCs |
| `infra/rule/` | Rule Engine implementation for conditional logic |
| `util/` | Technical utilities (currently empty, reserved for JSON/date utils) |

## For AI Agents

### Working In This Directory

**Value Objects (domain/):**
- Immutable UUID wrappers using Java records
- Factory methods: `generate()`, `of(UUID)`, `of(String)`
- Null safety with validation in compact constructor
- Used as aggregate identifiers across domain models

**Rule Engine (infra/rule/):**
- **Sealed Interface Pattern**: `Rule` is sealed, permits only `CompositeRule` and `LeafRule`
- **Composite Pattern**: Rules compose hierarchically (AND/OR logic)
- **Strategy Pattern**: Operators are functional interfaces
- **JSON Serialization**: Custom deserializer handles polymorphic Rule deserialization

**Rule Evaluation Flow:**
1. Parse JSON → `RuleDeserializer` → `Rule` instance
2. Validate → `RuleValidator.validate(rule)` → check structure/semantics
3. Build context → `RuleContext(attributes)` from domain object
4. Evaluate → `rule.evaluate(context)` → boolean result
5. Select → `PriceSelector.selectApplicable(prices, context)` → filtered list

**Attribute System:**
- **Enums Define Schema**: Each entity type has attribute enum (ProductGroupAttribute, etc.)
- **Type Safety**: Attributes are strongly typed, not magic strings
- **AttributeReader**: Reflective extractor pulls values from domain objects
- **AttributeDefinition**: Metadata about expected types (future validation)

**Design Decisions:**
- **Sealed Types**: Exhaustive pattern matching, compile-time safety
- **Immutability**: All Value Objects and Rules are immutable
- **Fail-Fast Validation**: RuleValidator catches errors before evaluation
- **Separation of Concerns**: Rule engine is domain-agnostic infrastructure

### Testing Requirements

**Unit Tests:**
- Value Objects: UUID generation, parsing, null handling
- Rule evaluation: CompositeRule AND/OR logic, LeafRule operators
- RuleValidator: Structure validation, semantic checks (unknown attributes)
- RuleOperators: Each operator implementation (EQUALS, IN, GREATER_THAN, etc.)
- AttributeReader: Extract values from domain objects

**Integration Tests:**
- JSON deserialization: Parse complex nested rules
- PriceSelector: End-to-end price selection with real domain objects
- Cross-entity rules: Rules referencing ProductGroup and Product attributes

**Test Data Patterns:**
```java
// Simple leaf rule
var rule = new LeafRule("product.type", "EQUALS", "SUBSCRIPTION");

// Composite rule
var rule = new CompositeRule("AND", List.of(
    new LeafRule("productGroup.status", "EQUALS", "ACTIVE"),
    new LeafRule("product.billingType", "EQUALS", "RECURRING")
));

// Context
var context = new RuleContext(Map.of(
    "product.type", "SUBSCRIPTION",
    "product.billingType", "RECURRING"
));
```

### Common Patterns

**Creating Value Objects:**
```java
// Generate new ID
ProductGroupId id = ProductGroupId.generate();

// Parse from string
ProductId productId = ProductId.of("550e8400-e29b-41d4-a716-446655440000");

// Parse from UUID
PriceId priceId = PriceId.of(uuid);
```

**Rule Construction:**
```java
// Leaf rule (attribute operator value)
var rule = new LeafRule("product.type", "EQUALS", "SUBSCRIPTION");

// Composite rule (AND/OR + list of sub-rules)
var rule = new CompositeRule("AND", List.of(
    new LeafRule(...),
    new CompositeRule("OR", List.of(...))
));
```

**Rule Validation:**
```java
var result = RuleValidator.validate(rule);
if (!result.valid()) {
    throw new IllegalArgumentException("Invalid rule: " + result.errors());
}
```

**Rule Evaluation:**
```java
// Build context from domain object
Map<String, Object> attrs = AttributeReader.readAttributes(productGroup, product, price);
var context = new RuleContext(attrs);

// Evaluate
boolean matches = rule.evaluate(context);
```

**Price Selection:**
```java
// Select all applicable prices
List<Price> applicable = PriceSelector.selectApplicable(allPrices, context);

// Select default price
Optional<Price> defaultPrice = applicable.stream()
    .filter(Price::isDefault)
    .findFirst();
```

**JSON Deserialization:**
```java
// Jackson automatically uses RuleDeserializer
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new SimpleModule()
    .addDeserializer(Rule.class, new RuleDeserializer()));

Rule rule = mapper.readValue(jsonString, Rule.class);
```

## Dependencies

### Internal

None (this is the foundation module)

### External

- Java 21: Records, sealed types, pattern matching
- Jackson: JSON serialization/deserialization
- SLF4J: Logging
