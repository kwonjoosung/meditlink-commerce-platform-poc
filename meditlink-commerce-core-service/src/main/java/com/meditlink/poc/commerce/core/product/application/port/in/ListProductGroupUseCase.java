package com.meditlink.poc.commerce.core.product.application.port.in;

import com.meditlink.poc.commerce.core.product.domain.ProductGroup;
import java.util.List;

// inbound port: 상품 그룹 목록 조회
public interface ListProductGroupUseCase {

    List<ProductGroup> listGroups();
}
