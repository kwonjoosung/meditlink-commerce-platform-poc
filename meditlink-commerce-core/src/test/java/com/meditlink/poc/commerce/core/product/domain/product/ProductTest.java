package com.meditlink.poc.commerce.core.product.domain.product;

import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private final ProductGroupId pgId = ProductGroupId.generate();

    @Test
    @DisplayName("생성 시 ACTIVE 상태, 기본값 설정")
    void create_setsDefaults() {
        var product = Product.create(pgId, "Pro Plan", "Pro Plan Display", "설명", ItemType.SUBSCRIPTION);

        assertNotNull(product.getProductId());
        assertEquals(pgId, product.getProductGroupId());
        assertNull(product.getExternalId());
        assertEquals("Pro Plan", product.getName());
        assertEquals(ItemType.SUBSCRIPTION, product.getItemType());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertTrue(product.getFeatures().isEmpty());
    }

    @Test
    @DisplayName("itemType이 null이면 생성 실패")
    void create_nullItemType_throws() {
        assertThrows(NullPointerException.class,
                () -> Product.create(pgId, "Test", null, null, null));
    }

    @Test
    @DisplayName("name이 null이면 생성 실패")
    void create_nullName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> Product.create(pgId, null, null, null, ItemType.SUBSCRIPTION));
    }

    @Test
    @DisplayName("productGroupId가 null이면 생성 실패")
    void create_nullProductGroupId_throws() {
        assertThrows(NullPointerException.class,
                () -> Product.create(null, "Test", null, null, ItemType.SUBSCRIPTION));
    }

    @Nested
    @DisplayName("Feature 관리")
    class FeatureTests {

        @Test
        void addFeature_success() {
            var product = Product.create(pgId, "Pro", null, null, ItemType.SUBSCRIPTION);
            product.addFeature("storage", 50L * 1024 * 1024 * 1024, "Storage 50GB", false);

            assertEquals(1, product.getFeatures().size());
            var feature = product.getFeature("storage");
            assertTrue(feature.isPresent());
            assertEquals(50L * 1024 * 1024 * 1024, feature.get().getQuota());
        }

        @Test
        void addFeature_duplicateCode_throws() {
            var product = Product.create(pgId, "Pro", null, null, ItemType.SUBSCRIPTION);
            product.addFeature("storage", 50L, null, false);

            assertThrows(IllegalStateException.class,
                    () -> product.addFeature("storage", 100L, null, false));
        }

        @Test
        void addFeature_blankCode_throws() {
            var product = Product.create(pgId, "Pro", null, null, ItemType.SUBSCRIPTION);
            assertThrows(IllegalArgumentException.class,
                    () -> product.addFeature("", null, null, false));
        }

        @Test
        void removeFeature_success() {
            var product = Product.create(pgId, "Pro", null, null, ItemType.SUBSCRIPTION);
            product.addFeature("storage", 50L, null, false);
            product.removeFeature("storage");

            assertTrue(product.getFeatures().isEmpty());
        }

        @Test
        void removeFeature_notFound_throws() {
            var product = Product.create(pgId, "Pro", null, null, ItemType.SUBSCRIPTION);
            assertThrows(IllegalStateException.class,
                    () -> product.removeFeature("nonexistent"));
        }

        @Test
        void getFeature_notFound_returnsEmpty() {
            var product = Product.create(pgId, "Pro", null, null, ItemType.SUBSCRIPTION);
            assertTrue(product.getFeature("nonexistent").isEmpty());
        }

        @Test
        void features_unmodifiable() {
            var product = Product.create(pgId, "Pro", null, null, ItemType.SUBSCRIPTION);
            product.addFeature("storage", 50L, null, false);

            assertThrows(UnsupportedOperationException.class,
                    () -> product.getFeatures().clear());
        }
    }

    @Nested
    @DisplayName("도메인 메서드")
    class DomainMethodTests {

        @Test
        void assignExternalId() {
            var product = Product.create(pgId, "Pro", null, null, ItemType.SUBSCRIPTION);
            product.assignExternalId("prod_abc123");
            assertEquals("prod_abc123", product.getExternalId());
        }

        @Test
        void assignExternalId_blank_throws() {
            var product = Product.create(pgId, "Pro", null, null, ItemType.SUBSCRIPTION);
            assertThrows(IllegalArgumentException.class,
                    () -> product.assignExternalId(""));
        }

        @Test
        void deactivate_and_activate() {
            var product = Product.create(pgId, "Pro", null, null, ItemType.SUBSCRIPTION);
            product.deactivate();
            assertEquals(ProductStatus.INACTIVE, product.getStatus());

            product.activate();
            assertEquals(ProductStatus.ACTIVE, product.getStatus());
        }

        @Test
        void isRecurring_subscription() {
            var subscription = Product.create(pgId, "Pro", null, null, ItemType.SUBSCRIPTION);
            assertTrue(subscription.isRecurring());
        }

        @Test
        void isRecurring_addOn() {
            var addOn = Product.create(pgId, "Add-On", null, null, ItemType.ADD_ON);
            assertTrue(addOn.isRecurring());
        }

        @Test
        void isRecurring_oneTime() {
            var oneTime = Product.create(pgId, "Setup", null, null, ItemType.ONE_TIME);
            assertFalse(oneTime.isRecurring());
        }
    }
}
