package com.meditlink.poc.commerce.core.shared.infra.rule;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 가격 선택 로직.
 * Price의 condition(Rule DSL)과 attributes(key-value) 두 가지 매칭 수단을 사용한다.
 *
 * <p>Price 데이터는 인터페이스로 추상화하여 도메인 모델에 대한 직접 의존을 피한다.</p>
 */
public final class PriceSelector {

    // 매칭에 사용하지 않는 attribute 키 (메타 속성)
    private static final Set<String> NON_MATCHING_KEYS = Set.of("priority", "discount_reason");

    private PriceSelector() {
    }

    /**
     * 가격 매칭에 필요한 정보를 추상화하는 인터페이스.
     * 도메인 Price 클래스가 이를 구현한다.
     */
    public interface PriceCandidate {
        String getCurrency();
        boolean isDefault();
        Rule getCondition();
        Map<String, Object> getAttributes();
    }

    /**
     * 주어진 후보 가격 목록에서 context에 맞는 가격을 선택한다.
     *
     * @param prices   후보 가격 목록
     * @param currency 적용 통화
     * @param context  평가 입력값
     * @return 매칭된 가격 (없으면 empty)
     */
    public static <T extends PriceCandidate> Optional<T> selectPrice(
            List<T> prices,
            String currency,
            RuleContext context) {

        // 1. 통화 필터
        List<T> candidates = prices.stream()
                .filter(p -> p.getCurrency().equals(currency))
                .toList();

        // 2. 조건부 가격 평가 (isDefault=false인 것들, priority 오름차순)
        List<T> conditionalPrices = candidates.stream()
                .filter(p -> !p.isDefault())
                .sorted(Comparator.comparingInt(PriceSelector::getPriority))
                .toList();

        for (T price : conditionalPrices) {
            if (matchesPrice(price, context)) {
                return Optional.of(price);
            }
        }

        // 3. fallback: isDefault=true
        return candidates.stream()
                .filter(PriceCandidate::isDefault)
                .findFirst();
    }

    private static boolean matchesPrice(PriceCandidate price, RuleContext context) {
        // 우선순위 1: condition이 있으면 condition으로 평가
        if (price.getCondition() != null) {
            return RuleEngine.evaluate(price.getCondition(), context);
        }

        // 우선순위 2: attributes 매칭
        Map<String, Object> attrs = price.getAttributes();
        if (attrs == null || attrs.isEmpty()) {
            return true;
        }

        for (Map.Entry<String, Object> entry : attrs.entrySet()) {
            if (NON_MATCHING_KEYS.contains(entry.getKey())) continue;

            Object contextValue = context.get(entry.getKey());
            if (!Objects.equals(contextValue, entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static int getPriority(PriceCandidate price) {
        Map<String, Object> attrs = price.getAttributes();
        if (attrs != null) {
            Object priority = attrs.get("priority");
            if (priority instanceof Number n) return n.intValue();
        }
        return 99;
    }
}
