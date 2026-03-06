package com.meditlink.poc.commerce.core.product.domain.productgroup;

import com.meditlink.poc.commerce.core.shared.infra.rule.LeafRule;
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
        var pg = ProductGroup.create("Design Suite", "design-suite", "설명");

        assertNotNull(pg.getProductGroupId());
        assertEquals("Design Suite", pg.getName());
        assertEquals("design-suite", pg.getSlug());
        assertEquals("설명", pg.getDescription());
        assertEquals(ProductGroupStatus.DRAFT, pg.getStatus());
        assertEquals(0, pg.getDisplayOrder());
        assertNull(pg.getCondition());
        assertTrue(pg.getAttributes().isEmpty());
        assertTrue(pg.getMetadata().isEmpty());
        assertTrue(pg.getTags().isEmpty());
        assertNotNull(pg.getCreatedAt());
        assertNotNull(pg.getUpdatedAt());
    }

    @Test
    @DisplayName("name이 null이면 생성 실패")
    void create_nullName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ProductGroup.create(null, null, null));
    }

    @Test
    @DisplayName("name이 빈 문자열이면 생성 실패")
    void create_blankName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ProductGroup.create("  ", null, null));
    }

    @Nested
    @DisplayName("상태 전이")
    class StatusTransitionTests {

        @Test
        void activate_fromDraft() {
            var pg = ProductGroup.create("Test", null, null);
            pg.activate();
            assertEquals(ProductGroupStatus.ACTIVE, pg.getStatus());
        }

        @Test
        void archive_fromActive() {
            var pg = ProductGroup.create("Test", null, null);
            pg.activate();
            pg.archive();
            assertEquals(ProductGroupStatus.ARCHIVED, pg.getStatus());
        }

        @Test
        void reactivate_fromArchived() {
            var pg = ProductGroup.create("Test", null, null);
            pg.activate();
            pg.archive();
            pg.reactivate();
            assertEquals(ProductGroupStatus.ACTIVE, pg.getStatus());
        }

        @Test
        void reactivate_fromDraft_throws() {
            var pg = ProductGroup.create("Test", null, null);
            assertThrows(IllegalStateException.class, pg::reactivate);
        }
    }

    @Test
    @DisplayName("updateInfo: name 업데이트")
    void updateInfo_updatesFields() {
        var pg = ProductGroup.create("Old", null, null);
        pg.updateInfo("New", "new-slug", "새 설명");

        assertEquals("New", pg.getName());
        assertEquals("new-slug", pg.getSlug());
        assertEquals("새 설명", pg.getDescription());
    }

    @Test
    @DisplayName("updateInfo: name이 빈 값이면 실패")
    void updateInfo_blankName_throws() {
        var pg = ProductGroup.create("Test", null, null);
        assertThrows(IllegalArgumentException.class,
                () -> pg.updateInfo("", null, null));
    }

    @Test
    @DisplayName("updateCondition: 유효한 Rule 설정")
    void updateCondition_validRule() {
        var pg = ProductGroup.create("Test", null, null);
        pg.updateCondition(new LeafRule("region", "EQ", "US"));
        assertNotNull(pg.getCondition());
    }

    @Test
    @DisplayName("updateCondition: 잘못된 Rule은 실패")
    void updateCondition_invalidRule_throws() {
        var pg = ProductGroup.create("Test", null, null);
        assertThrows(IllegalArgumentException.class,
                () -> pg.updateCondition(new LeafRule(null, "EQ", "US")));
    }

    @Test
    @DisplayName("updateCondition: null → 조건 제거")
    void updateCondition_null_clearsCondition() {
        var pg = ProductGroup.create("Test", null, null);
        pg.updateCondition(new LeafRule("region", "EQ", "US"));
        pg.updateCondition(null);
        assertNull(pg.getCondition());
    }

    @Test
    @DisplayName("attributes, metadata, tags 업데이트")
    void updateCollections() {
        var pg = ProductGroup.create("Test", null, null);

        pg.updateAttributes(Map.of("key", "value"));
        assertEquals("value", pg.getAttributes().get("key"));

        pg.updateMetadata(Map.of("meta", "data"));
        assertEquals("data", pg.getMetadata().get("meta"));

        pg.updateTags(List.of("tag1", "tag2"));
        assertEquals(2, pg.getTags().size());
    }

    @Test
    @DisplayName("null 컬렉션은 빈 컬렉션으로 대체")
    void updateCollections_null_becomesEmpty() {
        var pg = ProductGroup.create("Test", null, null);
        pg.updateAttributes(null);
        pg.updateMetadata(null);
        pg.updateTags(null);

        assertTrue(pg.getAttributes().isEmpty());
        assertTrue(pg.getMetadata().isEmpty());
        assertTrue(pg.getTags().isEmpty());
    }
}
