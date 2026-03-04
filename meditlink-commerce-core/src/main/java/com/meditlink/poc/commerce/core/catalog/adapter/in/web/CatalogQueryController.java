package com.meditlink.poc.commerce.core.catalog.adapter.in.web;

import com.meditlink.poc.commerce.core.catalog.application.port.in.ListCatalogUseCase;
import com.meditlink.poc.commerce.core.catalog.domain.CatalogItem;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog/items")
public class CatalogQueryController {

    private final ListCatalogUseCase listCatalogUseCase;

    public CatalogQueryController(ListCatalogUseCase listCatalogUseCase) {
        this.listCatalogUseCase = listCatalogUseCase;
    }

    @GetMapping
    public List<CatalogItem> listCatalogItems() {
        return listCatalogUseCase.listItems();
    }
}
