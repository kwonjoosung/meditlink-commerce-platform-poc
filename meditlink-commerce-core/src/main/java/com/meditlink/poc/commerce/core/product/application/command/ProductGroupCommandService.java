package com.meditlink.poc.commerce.core.product.application.command;

import com.meditlink.poc.commerce.core.product.application.dto.CreateProductGroupCommand;
import com.meditlink.poc.commerce.core.product.application.dto.UpdateProductGroupCommand;
import com.meditlink.poc.commerce.core.product.application.port.ProductGroupRepository;
import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroup;
import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductGroupCommandService {

    private final ProductGroupRepository repository;

    public ProductGroupCommandService(ProductGroupRepository repository) {
        this.repository = repository;
    }

    public ProductGroup create(CreateProductGroupCommand cmd) {
        var pg = ProductGroup.create(cmd.name(), cmd.slug(), cmd.description());
        if (cmd.attributes() != null) pg.updateAttributes(cmd.attributes());
        if (cmd.metadata() != null) pg.updateMetadata(cmd.metadata());
        if (cmd.tags() != null) pg.updateTags(cmd.tags());
        return repository.save(pg);
    }

    public ProductGroup update(String id, UpdateProductGroupCommand cmd) {
        var pg = repository.findById(ProductGroupId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("ProductGroup을 찾을 수 없습니다: " + id));

        pg.updateInfo(cmd.name(), cmd.slug(), cmd.description());
        if (cmd.attributes() != null) pg.updateAttributes(cmd.attributes());
        if (cmd.metadata() != null) pg.updateMetadata(cmd.metadata());
        if (cmd.tags() != null) pg.updateTags(cmd.tags());
        return repository.save(pg);
    }

    public ProductGroup activate(String id) {
        var pg = findOrThrow(id);
        pg.activate();
        return repository.save(pg);
    }

    public ProductGroup archive(String id) {
        var pg = findOrThrow(id);
        pg.archive();
        return repository.save(pg);
    }

    public void delete(String id) {
        var pg = findOrThrow(id);
        if (!pg.isDraft()) {
            throw new IllegalStateException("DRAFT 상태에서만 삭제할 수 있습니다");
        }
        repository.deleteById(pg.getProductGroupId());
    }

    private ProductGroup findOrThrow(String id) {
        return repository.findById(ProductGroupId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("ProductGroup을 찾을 수 없습니다: " + id));
    }
}
