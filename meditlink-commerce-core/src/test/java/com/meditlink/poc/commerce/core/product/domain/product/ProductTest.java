package com.meditlink.poc.commerce.core.product.domain.product;

import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;
import com.meditlink.poc.commerce.core.shared.infra.rule.LeafRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private final ProductGroupId pgId = ProductGroupId.generate();

    @Test
    @DisplayName("생성 시 ACTIVE 상태, 기본값 설정")
    void create_setsDefaults() {
        var product = Product.create(pgId, "Pro Plan", "설명", "PLAN", "RECURRING");

        assertNotNull(product.getProductId());
        assertEquals(pgId, product.getProductGroupId());
        assertNull(product.getExternalId());
        assertEquals("Pro Plan", product.getName());
        assertEquals("PLAN", product.getType());
        assertEquals("RECURRING", product.getBillingType());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertTrue(product.getFeatures().isEmpty());
    }

    @Test
    @DisplayName("잘못된 billingType이면 생성 실패")
    void create_invalidBillingType_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> Product.create(pgId, "Test", null, "PLAN", "INVALID"));
    }

    @Test
    @DisplayName("name이 null이면 생성 실패")
    void create_nullName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> Product.create(pgId, null, null, "PLAN", "RECURRING"));
    }

    @Test
    @DisplayName("productGroupId가 null이면 생성 실패")
    void create_nullProductGroupId_throws() {
        assertThrows(NullPointerException.class,
                () -> Product.create(null, "Test", null, "PLAN", "RECURRING"));
    }

    @Nested
    @DisplayName("Feature 관리")
    class FeatureTests {

        @Test
        void addFeature_success() {
            var product = Product.create(pgId, "Pro", null, "PLAN", "RECURRING");
            product.addFeature("storage", 50L * 1024 * 1024 * 1024, Map.of());

            assertEquals(1, product.getFeatures().size());
            var feature = product.getFeature("storage");
            assertTrue(feature.isPresent());
            assertEquals(50L * 1024 * 1024 * 1024, feature.get().getQuota());
        }

        @Test
        void addFeature_duplicateCode_throws() {
            var product = Product.create(pgId, "Pro", null, "PLAN", "RECURRING");
            product.addFeature("storage", 50L, null);

            assertThrows(IllegalStateException.class,
                    () -> product.addFeature("storage", 100L, null));
        }

        @Test
        void addFeature_blankCode_throws() {
            var product = Product.create(pgId, "Pro", null, "PLAN", "RECURRING");
            assertThrows(IllegalArgumentException.class,
                    () -> product.addFeature("", null, null));
        }

        @Test
        void removeFeature_success() {
            var product = Product.create(pgId, "Pro", null, "PLAN", "RECURRING");
            product.addFeature("storage", 50L, null);
            product.removeFeature("storage");

            assertTrue(product.getFeatures().isEmpty());
        }

        @Test
        void removeFeature_notFound_throws() {
            var product = Product.create(pgId, "Pro", null, "PLAN", "RECURRING");
            assertThrows(IllegalStateException.class,
                    () -> product.removeFeature("nonexistent"));
        }

        @Test
        void getFeature_notFound_returnsEmpty() {
            var product = Product.create(pgId, "Pro", null, "PLAN", "RECURRING");
            assertTrue(product.getFeature("nonexistent").isEmpty());
        }

        @Test
        void features_unmodifiable() {
            var product = Product.create(pgId, "Pro", null, "PLAN", "RECURRING");
            product.addFeature("storage", 50L, null);

            assertThrows(UnsupportedOperationException.class,
                    () -> product.getFeatures().clear());
        }
    }

    @Nested
    @DisplayName("도메인 메서드")
    class DomainMethodTests {

        @Test
        void assignExternalId() {
            var product = Product.create(pgId, "Pro", null, "PLAN", "RECURRING");
            product.assignExternalId("prod_abc123");
            assertEquals("prod_abc123", product.getExternalId());
        }

        @Test
        void assignExternalId_blank_throws() {
            var product = Product.create(pgId, "Pro", null, "PLAN", "RECURRING");
            assertThrows(IllegalArgumentException.class,
                    () -> product.assignExternalId(""));
        }

        @Test
        void deactivate_and_activate() {
            var product = Product.create(pgId, "Pro", null, "PLAN", "RECURRING");
            product.deactivate();
            assertEquals(ProductStatus.INACTIVE, product.getStatus());

            product.activate();
            assertEquals(ProductStatus.ACTIVE, product.getStatus());
        }

        @Test
        void updateCondition_valid() {
            var product = Product.create(pgId, "Pro", null, "PLAN", "RECURRING");
            product.updateCondition(new LeafRule("region", "EQ", "US"));
            assertNotNull(product.getCondition());
        }

        @Test
        void updateCondition_invalid_throws() {
            var product = Product.create(pgId, "Pro", null, "PLAN", "RECURRING");
            assertThrows(IllegalArgumentException.class,
                    () -> product.updateCondition(new LeafRule(null, "EQ", "US")));
        }

        @Test
        void isRecurring() {
            var recurring = Product.create(pgId, "Pro", null, "PLAN", "RECURRING");
            assertTrue(recurring.isRecurring());

            var oneTime = Product.create(pgId, "Setup", null, "ADDON", "ONE_TIME");
            assertFalse(oneTime.isRecurring());
        }
    }
}
