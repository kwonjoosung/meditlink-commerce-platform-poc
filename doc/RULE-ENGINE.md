# RULE-ENGINE.md — Rule Engine 설계

## 1. 개요

Rule Engine은 Shared 모듈에 위치하는 공통 인프라다.
JSONB로 저장된 조건(Rule)을 런타임에 평가하여 Boolean 결과를 반환한다.
Product의 상품 노출 조건, 가격 적용 조건 등에 사용된다.

### 위치

`shared/infra/rule/` 패키지

### 사용처

| 사용처 | Rule 필드 | 평가 결과 | 호출자 |
|--------|----------|----------|--------|
| Catalog.condition | 카탈로그 노출 조건 | Boolean | BFF |
| Product.condition | 상품 노출/구매 조건 | Boolean | BFF |
| Price.condition | 가격 적용 조건 | Boolean | BFF (PriceSelector 경유) |
| Price.attributes | 가격 속성 매칭 | Boolean | BFF (PriceSelector 경유) |
| Coupon.eligibility | 쿠폰 사용 조건 | Boolean | BFF 또는 Coupon 모듈 |

---

## 2. Rule 구조

### 2.1 타입

Rule은 두 가지 형태가 있다. JSONB에 저장될 때 `type` 필드 또는 `field` 필드 존재 여부로 구분.

```
Rule = CompositeRule | LeafRule | null

null → 조건 없음 (항상 true)
```

### 2.2 LeafRule

단일 조건. 하나의 필드를 하나의 연산자로 평가.

```json
{
  "field": "region",
  "op": "EQ",
  "value": "US"
}
```

```java
public record LeafRule(
    String field,      // RuleContext에서 꺼낼 키
    String op,         // 연산자 (RuleOperator enum)
    Object value       // 비교 대상 값
) implements Rule {}
```

**판별 기준:** `field` 키가 존재하면 LeafRule.

### 2.3 CompositeRule

복합 조건. 여러 Rule을 AND/OR/NOT으로 조합.

```json
{
  "type": "AND",
  "rules": [
    { "field": "region", "op": "IN", "value": ["US", "EU"] },
    { "field": "customer_tags", "op": "CONTAINS", "value": "enterprise" }
  ]
}
```

```java
public record CompositeRule(
    String type,           // "AND", "OR", "NOT"
    List<Rule> rules       // 하위 Rule 목록 (재귀)
) implements Rule {}
```

**판별 기준:** `type` 키가 `AND`, `OR`, `NOT` 중 하나면 CompositeRule.

### 2.4 JSON 역직렬화

JSONB → Rule 변환 시 판별 로직:

```java
public class RuleDeserializer {
    public static Rule deserialize(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.has("field")) return parseLeafRule(node);
        if (node.has("type")) return parseCompositeRule(node);
        throw new InvalidRuleException("Unknown rule structure");
    }
}
```

---

## 3. 연산자

### 3.1 지원 연산자 목록

```java
public enum RuleOperator {
    // 비교
    EQ,         // 같다          (단일값 비교)
    NEQ,        // 다르다        (단일값 비교)
    GT,         // 크다          (숫자)
    GTE,        // 이상          (숫자)
    LT,         // 작다          (숫자)
    LTE,        // 이하          (숫자)

    // 목록 포함
    IN,         // 값이 목록에 있다      (값, [목록])
    NOT_IN,     // 값이 목록에 없다      (값, [목록])

    // 리스트 연산
    CONTAINS,       // 리스트가 값을 포함       ([리스트], 값)
    CONTAINS_ANY,   // 리스트가 값 중 하나 포함  ([리스트], [값들])
    CONTAINS_ALL,   // 리스트가 값을 모두 포함   ([리스트], [값들])

    // 존재
    EXISTS,     // 값이 존재하는가    (필드 존재 + non-null)
    IS_EMPTY    // 비어있는가        (null, 빈문자열, 빈리스트)
}
```

### 3.2 연산자별 타입 기대

| 연산자 | context 값 타입 | rule value 타입 | 예시 |
|--------|----------------|----------------|------|
| EQ/NEQ | any | same as context | `region EQ "US"` |
| GT/GTE/LT/LTE | Number | Number | `subscription_months GTE 6` |
| IN/NOT_IN | any (단일값) | List | `region IN ["US", "EU"]` |
| CONTAINS | List | any (단일값) | `tags CONTAINS "enterprise"` |
| CONTAINS_ANY | List | List | `products CONTAINS_ANY ["pro"]` |
| CONTAINS_ALL | List | List | `features CONTAINS_ALL ["a", "b"]` |
| EXISTS | any | Boolean (ignored) | `custom_field EXISTS true` |
| IS_EMPTY | any | Boolean (ignored) | `tags IS_EMPTY true` |

### 3.3 연산자 구현

```java
public class RuleOperators {
    public static boolean apply(RuleOperator op, Object contextValue, Object ruleValue) {
        return switch (op) {
            case EQ -> Objects.equals(contextValue, ruleValue);
            case NEQ -> !Objects.equals(contextValue, ruleValue);
            case GT -> compareNumbers(contextValue, ruleValue) > 0;
            case GTE -> compareNumbers(contextValue, ruleValue) >= 0;
            case LT -> compareNumbers(contextValue, ruleValue) < 0;
            case LTE -> compareNumbers(contextValue, ruleValue) <= 0;
            case IN -> asList(ruleValue).contains(contextValue);
            case NOT_IN -> !asList(ruleValue).contains(contextValue);
            case CONTAINS -> asList(contextValue).contains(ruleValue);
            case CONTAINS_ANY -> hasAnyIntersection(asList(contextValue), asList(ruleValue));
            case CONTAINS_ALL -> asList(contextValue).containsAll(asList(ruleValue));
            case EXISTS -> contextValue != null;
            case IS_EMPTY -> isEmpty(contextValue);
        };
    }
}
```

---

## 4. RuleContext

평가 입력값. BFF가 여러 모듈에서 데이터를 수집해 조립하는 `Map<String, Object>`.

```java
public class RuleContext {
    private final Map<String, Object> data;

    public RuleContext(Map<String, Object> data) {
        this.data = Collections.unmodifiableMap(data);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }
}
```

### BFF에서의 조립 예시

```java
// BFF (Gateway)
Map<String, Object> contextData = new HashMap<>();
contextData.put("customer_id", customerId);
contextData.put("region", customer.getRegion());
contextData.put("customer_tags", customer.getTags());              // List<String>
contextData.put("active_products", subscriptions.getProductIds()); // List<String>
contextData.put("subscription_months", subscriptions.maxMonths()); // Integer
contextData.put("currency", resolveCurrency(customer.getRegion()));

RuleContext context = new RuleContext(contextData);
```

### 알려진 키 목록 (참고용, 강제하지 않음)

| 키 | 타입 | 출처 | 설명 |
|----|------|------|------|
| `customer_id` | String | Customer | 고객 식별자 |
| `region` | String | Customer | 고객 지역 |
| `customer_tags` | List\<String\> | Customer | 고객 태그 |
| `active_products` | List\<String\> | Billing | 현재 구독 중인 상품 ID |
| `active_features` | List\<String\> | Feature | 현재 활성 기능 코드 |
| `subscription_months` | Integer | Billing | 최장 구독 개월 수 |
| `currency` | String | 파생 | 적용 통화 |

---

## 5. RuleEngine

### 5.1 핵심 평가 로직

```java
public class RuleEngine {

    public static boolean evaluate(Rule rule, RuleContext context) {
        if (rule == null) return true; // 조건 없음 = 통과

        if (rule instanceof LeafRule leaf) {
            return evaluateLeaf(leaf, context);
        }

        if (rule instanceof CompositeRule composite) {
            return evaluateComposite(composite, context);
        }

        throw new IllegalArgumentException("Unknown rule type: " + rule.getClass());
    }

    private static boolean evaluateLeaf(LeafRule rule, RuleContext context) {
        Object contextValue = context.get(rule.field());
        if (contextValue == null) {
            // 필드가 context에 없으면:
            // EXISTS → false, IS_EMPTY → true, 나머지 → false
            return rule.op().equals("EXISTS") ? false :
                   rule.op().equals("IS_EMPTY") ? true : false;
        }
        RuleOperator op = RuleOperator.valueOf(rule.op());
        return RuleOperators.apply(op, contextValue, rule.value());
    }

    private static boolean evaluateComposite(CompositeRule rule, RuleContext context) {
        return switch (rule.type()) {
            case "AND" -> rule.rules().stream().allMatch(r -> evaluate(r, context));
            case "OR"  -> rule.rules().stream().anyMatch(r -> evaluate(r, context));
            case "NOT" -> !evaluate(rule.rules().get(0), context);
            default -> throw new IllegalArgumentException("Unknown composite type: " + rule.type());
        };
    }
}
```

### 5.2 설계 의도

- **순수 함수**: 부수효과 없음. 입력(Rule + Context)만으로 결과 결정.
- **null-safe**: rule이 null이면 true (조건 없음). context 값이 없으면 false (불충족).
- **재귀적**: CompositeRule 안에 CompositeRule이 들어갈 수 있음. 깊이 제한은 RuleValidator에서.
- **static 메서드**: 상태 없음. 인스턴스 불필요.

---

## 6. PriceSelector

### 6.1 가격 선택 로직

Price에는 `condition`(Rule DSL)과 `attributes`(key-value) 두 가지 매칭 수단이 있다.

```java
public class PriceSelector {

    public static Optional<Price> selectPrice(
            List<Price> prices,
            String currency,
            RuleContext context) {

        // 1. 통화 필터
        List<Price> candidates = prices.stream()
            .filter(p -> p.getCurrency().equals(currency))
            .toList();

        // 2. 조건부 가격 평가 (isDefault=false인 것들)
        List<Price> conditionalPrices = candidates.stream()
            .filter(p -> !p.isDefault())
            .sorted(Comparator.comparingInt(p -> getPriority(p)))
            .toList();

        for (Price price : conditionalPrices) {
            if (matchesPrice(price, context)) {
                return Optional.of(price);
            }
        }

        // 3. fallback: isDefault=true
        return candidates.stream()
            .filter(Price::isDefault)
            .findFirst();
    }

    private static boolean matchesPrice(Price price, RuleContext context) {
        // 우선순위 1: condition이 있으면 condition으로 평가
        if (price.getCondition() != null) {
            return RuleEngine.evaluate(price.getCondition(), context);
        }

        // 우선순위 2: attributes 매칭
        // attributes의 모든 key-value가 context와 일치해야 매칭
        Map<String, Object> attrs = price.getAttributes();
        if (attrs == null || attrs.isEmpty()) {
            return true; // 조건 없음 = 매칭
        }

        // attributes에서 매칭에 사용하지 않는 키 제외
        Set<String> nonMatchingKeys = Set.of("priority", "discount_reason");

        for (Map.Entry<String, Object> entry : attrs.entrySet()) {
            if (nonMatchingKeys.contains(entry.getKey())) continue;

            Object contextValue = context.get(entry.getKey());
            if (!Objects.equals(contextValue, entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static int getPriority(Price price) {
        Object priority = price.getAttributes().get("priority");
        if (priority instanceof Number n) return n.intValue();
        return 99; // default priority
    }
}
```

### 6.2 매칭 우선순위 정리

```
1. condition이 있는 Price → condition 평가 (RuleEngine)
2. condition이 없고 attributes에 매칭키가 있는 Price → attributes 키-값 비교
3. 둘 다 없는 Price → 무조건 매칭

여러 Price가 매칭되면 → priority(attributes 내) 오름차순으로 가장 높은 것
매칭되는 Price가 없으면 → isDefault=true인 Price (fallback)
default도 없으면 → Optional.empty() (이 통화 미지원)
```

### 6.3 attributes에서 매칭에 사용하지 않는 키

`priority`, `discount_reason` 등은 매칭 대상이 아닌 메타 속성이다.
이를 `nonMatchingKeys`로 명시적 제외한다.

> ⚠️ 정책 결정: nonMatchingKeys를 코드 상수로 관리할지, Attribute Definition에서 "matchable" 플래그로 관리할지는 구현 시 결정. PoC에서는 코드 상수로.

---

## 7. RuleValidator

Rule 저장 시 구조 검증.

```java
public class RuleValidator {

    private static final int MAX_DEPTH = 5;
    private static final Set<String> VALID_COMPOSITE_TYPES = Set.of("AND", "OR", "NOT");
    private static final Set<String> VALID_OPERATORS = Arrays.stream(RuleOperator.values())
        .map(Enum::name).collect(Collectors.toSet());

    public static ValidationResult validate(Rule rule) {
        List<String> errors = new ArrayList<>();
        validateRule(rule, 0, errors);
        return new ValidationResult(errors.isEmpty(), errors);
    }

    private static void validateRule(Rule rule, int depth, List<String> errors) {
        if (rule == null) return; // null은 유효 (조건 없음)

        if (depth > MAX_DEPTH) {
            errors.add("Rule nesting depth exceeds maximum of " + MAX_DEPTH);
            return;
        }

        if (rule instanceof LeafRule leaf) {
            validateLeaf(leaf, errors);
        } else if (rule instanceof CompositeRule composite) {
            validateComposite(composite, depth, errors);
        }
    }

    private static void validateLeaf(LeafRule rule, List<String> errors) {
        if (rule.field() == null || rule.field().isBlank()) {
            errors.add("LeafRule field is required");
        }
        if (rule.op() == null || !VALID_OPERATORS.contains(rule.op())) {
            errors.add("Invalid operator: " + rule.op());
        }
        // value는 null 허용 (EXISTS, IS_EMPTY 등)
    }

    private static void validateComposite(CompositeRule rule, int depth, List<String> errors) {
        if (!VALID_COMPOSITE_TYPES.contains(rule.type())) {
            errors.add("Invalid composite type: " + rule.type());
        }
        if (rule.rules() == null || rule.rules().isEmpty()) {
            errors.add("CompositeRule must have at least one sub-rule");
        }
        if ("NOT".equals(rule.type()) && rule.rules() != null && rule.rules().size() != 1) {
            errors.add("NOT rule must have exactly one sub-rule");
        }
        if (rule.rules() != null) {
            for (Rule sub : rule.rules()) {
                validateRule(sub, depth + 1, errors);
            }
        }
    }
}
```

---

## 8. Attribute Definition 기반 Default 처리

### 8.1 개요

attributes(JSONB)에 모든 키를 넣지 않아도 되도록, Attribute Definition에서 default 값을 제공.

### 8.2 AttributeReader

```java
public class AttributeReader {

    @SuppressWarnings("unchecked")
    public static <T> T get(Map<String, Object> attributes, AttributeDefinition def) {
        if (attributes != null && attributes.containsKey(def.getKey())) {
            return (T) attributes.get(def.getKey());
        }
        return (T) def.getDefaultValue();
    }
}

// 사용
int priority = AttributeReader.get(price.getAttributes(), PriceAttribute.PRIORITY);
// attributes에 priority가 없으면 → 99 (PriceAttribute.PRIORITY의 default)
```

### 8.3 설계 의도

- JSONB에는 **명시적으로 설정한 값만** 저장. default는 저장하지 않음.
- 읽을 때 AttributeReader가 default를 채움.
- 이렇게 하면 나중에 default 값을 바꿔도 기존 데이터에 영향 없음 (명시적 설정만 유지).
