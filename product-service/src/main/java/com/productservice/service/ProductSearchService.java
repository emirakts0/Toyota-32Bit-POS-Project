package com.productservice.service;

import com.productservice.dto.ImageDto;
import com.productservice.dto.ProductDto;
import com.productservice.dto.ProductSearchCriteria;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;

public interface ProductSearchService {


    Page<ProductDto> getProductsByCriteria(ProductSearchCriteria criteria);

    @Transactional
    Page<ProductDto> getProductsByPrefix(String prefix,
                                         int pageSize,
                                         int pageNumber,
                                         boolean hideDeleted);

    @Transactional
    ImageDto getProductImageByImageCode(Long imageCode);

    @Transactional
    ProductDto getProductByBarcode(String barcode);
}
