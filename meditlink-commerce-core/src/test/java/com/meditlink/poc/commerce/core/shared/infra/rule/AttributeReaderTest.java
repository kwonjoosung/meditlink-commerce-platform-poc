package com.meditlink.poc.commerce.core.shared.infra.rule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AttributeReaderTest {

    @Test
    @DisplayName("명시적 값이 있으면 그 값을 반환")
    void get_explicitValue_returnsIt() {
        Map<String, Object> attrs = Map.of("tier", 3);
        int tier = AttributeReader.get(attrs, ProductAttribute.TIER);
        assertEquals(3, tier);
    }

    @Test
    @DisplayName("키가 없으면 default 값 반환")
    void get_missingKey_returnsDefault() {
        Map<String, Object> attrs = Map.of();
        int tier = AttributeReader.get(attrs, ProductAttribute.TIER);
        assertEquals(0, tier); // ProductAttribute.TIER default = 0
    }

    @Test
    @DisplayName("null attributes면 default 값 반환")
    void get_nullAttributes_returnsDefault() {
        int priority = AttributeReader.get(null, PriceAttribute.PRIORITY);
        assertEquals(99, priority); // PriceAttribute.PRIORITY default = 99
    }

    @Test
    @DisplayName("default가 null인 attribute는 null 반환")
    void get_nullDefault_returnsNull() {
        Map<String, Object> attrs = Map.of();
        String audience = AttributeReader.get(attrs, PriceAttribute.AUDIENCE);
        assertNull(audience); // PriceAttribute.AUDIENCE default = null
    }

    @Test
    @DisplayName("ProductGroupAttribute default 동작")
    void get_productGroupAttribute_returnsDefault() {
        Map<String, Object> attrs = Map.of();
        String type = AttributeReader.get(attrs, ProductGroupAttribute.PRODUCT_GROUP_TYPE);
        assertEquals("general", type);
    }

    @Test
    @DisplayName("명시적으로 설정된 ProductGroupAttribute")
    void get_productGroupAttribute_explicitValue() {
        Map<String, Object> attrs = Map.of("display_style", "comparison_table");
        String style = AttributeReader.get(attrs, ProductGroupAttribute.DISPLAY_STYLE);
        assertEquals("comparison_table", style);
    }
}
