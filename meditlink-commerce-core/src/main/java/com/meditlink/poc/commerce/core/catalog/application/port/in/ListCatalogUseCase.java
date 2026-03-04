package com.meditlink.poc.commerce.core.catalog.application.port.in;

import com.meditlink.poc.commerce.core.catalog.domain.CatalogItem;
import java.util.List;

public interface ListCatalogUseCase {

    List<CatalogItem> listItems();
}
