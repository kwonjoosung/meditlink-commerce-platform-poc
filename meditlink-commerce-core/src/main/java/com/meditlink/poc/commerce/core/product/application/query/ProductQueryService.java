package com.meditlink.poc.commerce.core.product.application.query;

import com.meditlink.poc.commerce.core.product.application.port.PriceRepository;
import com.meditlink.poc.commerce.core.product.application.port.ProductGroupRepository;
import com.meditlink.poc.commerce.core.product.application.port.ProductRepository;
import com.meditlink.poc.commerce.core.product.domain.price.Price;
import com.meditlink.poc.commerce.core.product.domain.product.Product;
import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroup;
import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ProductGroupRepository productGroupRepository;
    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;

    public ProductQueryService(ProductGroupRepository productGroupRepository,
                               ProductRepository productRepository,
                               PriceRepository priceRepository) {
        this.productGroupRepository = productGroupRepository;
        this.productRepository = productRepository;
        this.priceRepository = priceRepository;
    }

    public List<ProductGroup> findAllProductGroups() {
        return productGroupRepository.findAll();
    }

    public Optional<ProductGroup> findProductGroupById(String id) {
        return productGroupRepository.findById(ProductGroupId.of(id));
    }

    public Optional<ProductGroup> findProductGroupBySlug(String slug) {
        return productGroupRepository.findBySlug(slug);
    }

    public List<Product> findProductsByProductGroupId(String productGroupId) {
        return productRepository.findByProductGroupId(ProductGroupId.of(productGroupId));
    }

    public Optional<Product> findProductById(String id) {
        return productRepository.findById(ProductId.of(id));
    }

    public List<Price> findPricesByProductId(String productId) {
        return priceRepository.findByProductId(ProductId.of(productId));
    }
}
