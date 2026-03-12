package com.meditlink.poc.commerce.core.product.application;

import com.meditlink.poc.commerce.core.product.application.command.PriceCommandService;
import com.meditlink.poc.commerce.core.product.application.command.ProductCommandService;
import com.meditlink.poc.commerce.core.product.application.command.ProductGroupCommandService;
import com.meditlink.poc.commerce.core.product.application.dto.*;
import com.meditlink.poc.commerce.core.product.application.port.*;
import com.meditlink.poc.commerce.core.product.application.query.ProductQueryService;
import com.meditlink.poc.commerce.core.product.domain.price.BillingPeriod;
import com.meditlink.poc.commerce.core.product.domain.price.Price;
import com.meditlink.poc.commerce.core.product.domain.product.ItemType;
import com.meditlink.poc.commerce.core.product.domain.product.Product;
import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroup;
import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroupStatus;
import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroupType;
import com.meditlink.poc.commerce.core.product.domain.product.ProductStatus;
import com.meditlink.poc.commerce.core.shared.domain.PriceId;
import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.meditlink.poc.commerce.core.product.infrastructure.kafka.ProductEventPublisher;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Application Service 통합 시나리오 테스트.
 * 인메모리 Repository + Stub Stripe로 전체 흐름 검증.
 */
class ProductScenarioTest {

    private ProductGroupCommandService pgService;
    private ProductCommandService productService;
    private PriceCommandService priceService;
    private ProductQueryService queryService;

    @BeforeEach
    void setup() {
        var pgRepo = new InMemoryProductGroupRepository();
        var productRepo = new InMemoryProductRepository();
        var priceRepo = new InMemoryPriceRepository();
        var stubProductSync = new StubSync();
        var stubPriceSync = new StubPriceSync();

        var stubEventPublisher = new NoOpEventPublisher();
        pgService = new ProductGroupCommandService(pgRepo);
        productService = new ProductCommandService(productRepo, pgRepo, stubProductSync, stubEventPublisher);
        priceService = new PriceCommandService(priceRepo, productRepo, stubPriceSync, stubEventPublisher);
        queryService = new ProductQueryService(pgRepo, productRepo, priceRepo);
    }

    @Test
    @DisplayName("전체 시나리오: ProductGroup 생성 → Product 생성 → Feature 추가 → Price 생성 → 조회")
    void fullScenario() {
        // 1. ProductGroup 생성
        var pg = pgService.create(new CreateProductGroupCommand(
                "Design Suite", "design-suite", "디자인 도구", ProductGroupType.PLAN_FAMILY));

        assertNotNull(pg.getProductGroupId());
        assertEquals(ProductGroupStatus.DRAFT, pg.getStatus());
        assertEquals(ProductGroupType.PLAN_FAMILY, pg.getType());

        // 2. ProductGroup 활성화
        pg = pgService.activate(pg.getProductGroupId().toString());
        assertEquals(ProductGroupStatus.ACTIVE, pg.getStatus());

        // 3. Product 생성
        var product = productService.create(new CreateProductCommand(
                pg.getProductGroupId().toString(),
                "Pro Plan", "프로 플랜", "전문가용 디자인 도구", ItemType.SUBSCRIPTION));

        assertNotNull(product.getProductId());
        assertNotNull(product.getExternalId()); // Stripe stub이 할당
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertEquals(ItemType.SUBSCRIPTION, product.getItemType());
        assertEquals("프로 플랜", product.getDisplayName());

        // 4. Feature 추가
        product = productService.addFeature(product.getProductId().toString(),
                new AddFeatureCommand("storage", 50L * 1024 * 1024 * 1024, "스토리지 50GB", true));
        assertEquals(1, product.getFeatures().size());
        assertEquals("storage", product.getFeatures().getFirst().getFeatureCode());

        product = productService.addFeature(product.getProductId().toString(),
                new AddFeatureCommand("design-editor", null, "디자인 에디터", false));
        assertEquals(2, product.getFeatures().size());

        // 5. Price 생성 (default, 월간)
        var price = priceService.create(new CreatePriceCommand(
                product.getProductId().toString(),
                "USD", 4900, BillingPeriod.MONTHLY, true));

        assertNotNull(price.getPriceId());
        assertNotNull(price.getExternalId());
        assertTrue(price.isDefault());
        assertEquals(4900, price.getAmount());
        assertEquals(BillingPeriod.MONTHLY, price.getBillingPeriod());

        // 6. 연간 가격 생성
        var yearlyPrice = priceService.create(new CreatePriceCommand(
                product.getProductId().toString(),
                "USD", 49000, BillingPeriod.YEARLY, false));

        assertFalse(yearlyPrice.isDefault());
        assertEquals(BillingPeriod.YEARLY, yearlyPrice.getBillingPeriod());

        // 7. 조회
        var foundPg = queryService.findProductGroupById(pg.getProductGroupId().toString());
        assertTrue(foundPg.isPresent());

        var products = queryService.findProductsByProductGroupId(pg.getProductGroupId().toString());
        assertEquals(1, products.size());

        var prices = queryService.findPricesByProductId(product.getProductId().toString());
        assertEquals(2, prices.size());

        var foundProduct = queryService.findProductById(product.getProductId().toString());
        assertTrue(foundProduct.isPresent());
        assertEquals(2, foundProduct.get().getFeatures().size());
    }

    @Test
    @DisplayName("존재하지 않는 ProductGroup으로 Product 생성 시 실패")
    void createProduct_invalidProductGroup_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                productService.create(new CreateProductCommand(
                        UUID.randomUUID().toString(),
                        "Test", "Test", null, ItemType.SUBSCRIPTION)));
    }

    @Test
    @DisplayName("DRAFT가 아닌 ProductGroup은 삭제 불가")
    void deleteProductGroup_notDraft_throws() {
        var pg = pgService.create(new CreateProductGroupCommand(
                "Test", null, null, ProductGroupType.PLAN_FAMILY));
        pgService.activate(pg.getProductGroupId().toString());

        assertThrows(IllegalStateException.class, () ->
                pgService.delete(pg.getProductGroupId().toString()));
    }

    // ── 인메모리 구현 ──

    static class InMemoryProductGroupRepository implements ProductGroupRepository {
        private final Map<UUID, ProductGroup> store = new ConcurrentHashMap<>();

        @Override
        public ProductGroup save(ProductGroup pg) {
            store.put(pg.getProductGroupId().value(), pg);
            return pg;
        }
        @Override
        public Optional<ProductGroup> findById(ProductGroupId id) {
            return Optional.ofNullable(store.get(id.value()));
        }
        @Override
        public Optional<ProductGroup> findBySlug(String slug) {
            return store.values().stream().filter(pg -> slug.equals(pg.getSlug())).findFirst();
        }
        @Override
        public List<ProductGroup> findAll() { return new ArrayList<>(store.values()); }
        @Override
        public void deleteById(ProductGroupId id) { store.remove(id.value()); }
    }

    static class InMemoryProductRepository implements ProductRepository {
        private final Map<UUID, Product> store = new ConcurrentHashMap<>();

        @Override
        public Product save(Product p) { store.put(p.getProductId().value(), p); return p; }
        @Override
        public Optional<Product> findById(ProductId id) { return Optional.ofNullable(store.get(id.value())); }
        @Override
        public List<Product> findByProductGroupId(ProductGroupId pgId) {
            return store.values().stream().filter(p -> p.getProductGroupId().equals(pgId)).toList();
        }
        @Override
        public void deleteById(ProductId id) { store.remove(id.value()); }
    }

    static class InMemoryPriceRepository implements PriceRepository {
        private final Map<UUID, Price> store = new ConcurrentHashMap<>();

        @Override
        public Price save(Price p) { store.put(p.getPriceId().value(), p); return p; }
        @Override
        public Optional<Price> findById(PriceId id) { return Optional.ofNullable(store.get(id.value())); }
        @Override
        public List<Price> findByProductId(ProductId productId) {
            return store.values().stream().filter(p -> p.getProductId().equals(productId)).toList();
        }
        @Override
        public void deleteById(PriceId id) { store.remove(id.value()); }
    }

    static class StubSync implements StripeProductSync {
        @Override
        public String syncProduct(Product p) { return "prod_" + p.getProductId().value().toString().substring(0, 8); }
    }

    static class StubPriceSync implements StripePriceSync {
        @Override
        public String syncPrice(Price p, String extId) { return "price_" + p.getPriceId().value().toString().substring(0, 8); }
    }

    static class NoOpEventPublisher extends ProductEventPublisher {
        NoOpEventPublisher() { super(null, "t1", "t2", "t3"); }
        @Override public void publishProductCreated(Product p) { /* no-op */ }
        @Override public void publishProductUpdated(Product p) { /* no-op */ }
        @Override public void publishPriceChanged(Price p, com.meditlink.poc.commerce.common.proto.v1.PriceChangedEvent.ChangeType ct) { /* no-op */ }
    }
}
