package com.meditlink.poc.commerce.core.product.domain.productgroup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProductGroupTest {

    @Test
    @DisplayName("생성 시 DRAFT 상태, 기본값 설정")
    void create_setsDefaults() {
        var pg = ProductGroup.create("Design Suite", "design-suite", "설명", ProductGroupType.PLAN_FAMILY);

        assertNotNull(pg.getProductGroupId());
        assertEquals("Design Suite", pg.getName());
        assertEquals("design-suite", pg.getSlug());
        assertEquals("설명", pg.getDescription());
        assertEquals(ProductGroupType.PLAN_FAMILY, pg.getType());
        assertEquals(ProductGroupStatus.DRAFT, pg.getStatus());
        assertEquals(0, pg.getSortOrder());
        assertTrue(pg.getTags().isEmpty());
        assertNotNull(pg.getCreatedAt());
        assertNotNull(pg.getUpdatedAt());
    }

    @Test
    @DisplayName("name이 null이면 생성 실패")
    void create_nullName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ProductGroup.create(null, null, null, ProductGroupType.PLAN_FAMILY));
    }

    @Test
    @DisplayName("name이 빈 문자열이면 생성 실패")
    void create_blankName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ProductGroup.create("  ", null, null, ProductGroupType.PLAN_FAMILY));
    }

    @Test
    @DisplayName("type이 null이면 생성 실패")
    void create_nullType_throws() {
        assertThrows(NullPointerException.class,
                () -> ProductGroup.create("Test", null, null, null));
    }

    @Nested
    @DisplayName("상태 전이")
    class StatusTransitionTests {

        @Test
        void activate_fromDraft() {
            var pg = ProductGroup.create("Test", null, null, ProductGroupType.PLAN_FAMILY);
            pg.activate();
            assertEquals(ProductGroupStatus.ACTIVE, pg.getStatus());
        }

        @Test
        void archive_fromActive() {
            var pg = ProductGroup.create("Test", null, null, ProductGroupType.PLAN_FAMILY);
            pg.activate();
            pg.archive();
            assertEquals(ProductGroupStatus.ARCHIVED, pg.getStatus());
        }

        @Test
        void reactivate_fromArchived() {
            var pg = ProductGroup.create("Test", null, null, ProductGroupType.PLAN_FAMILY);
            pg.activate();
            pg.archive();
            pg.reactivate();
            assertEquals(ProductGroupStatus.ACTIVE, pg.getStatus());
        }

        @Test
        void reactivate_fromDraft_throws() {
            var pg = ProductGroup.create("Test", null, null, ProductGroupType.PLAN_FAMILY);
            assertThrows(IllegalStateException.class, pg::reactivate);
        }
    }

    @Test
    @DisplayName("updateInfo: name 업데이트")
    void updateInfo_updatesFields() {
        var pg = ProductGroup.create("Old", null, null, ProductGroupType.PLAN_FAMILY);
        pg.updateInfo("New", "new-slug", "새 설명");

        assertEquals("New", pg.getName());
        assertEquals("new-slug", pg.getSlug());
        assertEquals("새 설명", pg.getDescription());
    }

    @Test
    @DisplayName("updateInfo: name이 빈 값이면 실패")
    void updateInfo_blankName_throws() {
        var pg = ProductGroup.create("Test", null, null, ProductGroupType.PLAN_FAMILY);
        assertThrows(IllegalArgumentException.class,
                () -> pg.updateInfo("", null, null));
    }

    @Test
    @DisplayName("tags 업데이트")
    void updateTags() {
        var pg = ProductGroup.create("Test", null, null, ProductGroupType.PLAN_FAMILY);

        pg.updateTags(List.of("tag1", "tag2"));
        assertEquals(2, pg.getTags().size());
    }

    @Test
    @DisplayName("null tags는 빈 컬렉션으로 대체")
    void updateTags_null_becomesEmpty() {
        var pg = ProductGroup.create("Test", null, null, ProductGroupType.PLAN_FAMILY);
        pg.updateTags(null);

        assertTrue(pg.getTags().isEmpty());
    }

    @Test
    @DisplayName("sortOrder 업데이트")
    void updateSortOrder() {
        var pg = ProductGroup.create("Test", null, null, ProductGroupType.ADD_ON_FAMILY);
        pg.updateSortOrder(5);
        assertEquals(5, pg.getSortOrder());
    }
}
