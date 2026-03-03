package com.meditlink.poc.commerce.core.catalog.application.service;

import com.meditlink.poc.commerce.core.catalog.application.port.in.ListCatalogUseCase;
import com.meditlink.poc.commerce.core.catalog.domain.CatalogItem;
import com.meditlink.poc.commerce.core.product.application.port.out.LoadProductPort;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogService implements ListCatalogUseCase {

    private final LoadProductPort loadProductPort;

    public CatalogService(LoadProductPort loadProductPort) {
        this.loadProductPort = loadProductPort;
    }

    @Override
    public List<CatalogItem> listItems() {
        return loadProductPort.loadAll().stream()
                .map(product -> new CatalogItem(product.id(), product.sku(), product.name()))
                .toList();
    }
}
