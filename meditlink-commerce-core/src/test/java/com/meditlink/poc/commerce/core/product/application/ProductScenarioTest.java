package com.meditlink.poc.commerce.core.product.application;

import com.meditlink.poc.commerce.core.product.application.command.PriceCommandService;
import com.meditlink.poc.commerce.core.product.application.command.ProductCommandService;
import com.meditlink.poc.commerce.core.product.application.command.ProductGroupCommandService;
import com.meditlink.poc.commerce.core.product.application.dto.*;
import com.meditlink.poc.commerce.core.product.application.port.*;
import com.meditlink.poc.commerce.core.product.application.query.ProductQueryService;
import com.meditlink.poc.commerce.core.product.domain.price.Price;
import com.meditlink.poc.commerce.core.product.domain.product.Product;
import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroup;
import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroupStatus;
import com.meditlink.poc.commerce.core.product.domain.product.ProductStatus;
import com.meditlink.poc.commerce.core.shared.domain.PriceId;
import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

        pgService = new ProductGroupCommandService(pgRepo);
        productService = new ProductCommandService(productRepo, pgRepo, stubProductSync);
        priceService = new PriceCommandService(priceRepo, productRepo, stubPriceSync);
        queryService = new ProductQueryService(pgRepo, productRepo, priceRepo);
    }

    @Test
    @DisplayName("전체 시나리오: ProductGroup 생성 → Product 생성 → Feature 추가 → Price 생성 → 조회")
    void fullScenario() {
        // 1. ProductGroup 생성
        var pg = pgService.create(new CreateProductGroupCommand(
                "Design Suite", "design-suite", "디자인 도구",
                Map.of("product_group_type", "plan_tier"), Map.of(), List.of("design")));

        assertNotNull(pg.getProductGroupId());
        assertEquals(ProductGroupStatus.DRAFT, pg.getStatus());

        // 2. ProductGroup 활성화
        pg = pgService.activate(pg.getProductGroupId().toString());
        assertEquals(ProductGroupStatus.ACTIVE, pg.getStatus());

        // 3. Product 생성
        var product = productService.create(new CreateProductCommand(
                pg.getProductGroupId().toString(),
                "Pro Plan", "프로 플랜", "PLAN", "RECURRING",
                null, Map.of("tier", 2), Map.of(), List.of("premium")));

        assertNotNull(product.getProductId());
        assertNotNull(product.getExternalId()); // Stripe stub이 할당
        assertEquals(ProductStatus.ACTIVE, product.getStatus());

        // 4. Feature 추가
        product = productService.addFeature(product.getProductId().toString(),
                new AddFeatureCommand("storage", 50L * 1024 * 1024 * 1024, Map.of()));
        assertEquals(1, product.getFeatures().size());
        assertEquals("storage", product.getFeatures().getFirst().getFeatureCode());

        product = productService.addFeature(product.getProductId().toString(),
                new AddFeatureCommand("design-editor", null, Map.of()));
        assertEquals(2, product.getFeatures().size());

        // 5. Price 생성 (default)
        var price = priceService.create(new CreatePriceCommand(
                product.getProductId().toString(),
                "USD", 4900, "MONTH", 1, true,
                null, Map.of(), Map.of(), List.of()));

        assertNotNull(price.getPriceId());
        assertNotNull(price.getExternalId());
        assertTrue(price.isDefault());
        assertEquals(4900, price.getAmount());

        // 6. 조건부 가격 생성
        var condPrice = priceService.create(new CreatePriceCommand(
                product.getProductId().toString(),
                "USD", 3900, "MONTH", 1, false,
                Map.of("field", "customer_tags", "op", "CONTAINS", "value", "enterprise"),
                Map.of("priority", 1, "discount_reason", "enterprise_discount"),
                Map.of(), List.of("enterprise")));

        assertFalse(condPrice.isDefault());
        assertNotNull(condPrice.getCondition());

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
                        "Test", null, "PLAN", "RECURRING",
                        null, null, null, null)));
    }

    @Test
    @DisplayName("DRAFT가 아닌 ProductGroup은 삭제 불가")
    void deleteProductGroup_notDraft_throws() {
        var pg = pgService.create(new CreateProductGroupCommand("Test", null, null, null, null, null));
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
}
