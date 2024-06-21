package com.productservice.service;

import com.productservice.dto.ImageDto;
import com.productservice.dto.ProductDto;
import com.productservice.dto.ProductSearchCriteria;
import com.productservice.exception.ImageNotFoundException;
import com.productservice.exception.InvalidInputException;
import com.productservice.exception.ProductNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;

/**
 * Service interface for searching and retrieving products.
 * Provides methods for searching products based on various criteria and retrieving product details.
 * @author Emir Aktaş
 */
public interface ProductSearchService {

    /**
     * Retrieves a product by its barcode.
     *
     * @param barcode the barcode of the product to be retrieved
     * @return the product data transfer object
     * @throws ProductNotFoundException if the product with the given barcode is not found or is marked as deleted
     */
    @Transactional
    ProductDto getProductByBarcode(String barcode);


    /**
     * Retrieves a paginated list of products whose names start with the given prefix.
     *
     * @param prefix      the prefix of the product names
     * @param pageSize    the size of the page to be returned
     * @param pageNumber  the number of the page to be returned
     * @param hideDeleted flag to hide deleted products
     * @return a paginated list of product data transfer objects
     * @throws InvalidInputException if any input parameters are invalid
     */
    @Transactional
    Page<ProductDto> getProductsByPrefix(String prefix,
                                         int pageSize,
                                         int pageNumber,
                                         boolean hideDeleted);


    /**
     * Retrieves the image associated with a product by its image code.
     *
     * @param imageCode the code of the image to be retrieved
     * @return the image data transfer object
     * @throws ImageNotFoundException if the image with the given code is not found
     * @throws ProductNotFoundException if the product associated with the image is not found or is marked as deleted
     */
    @Transactional
    ImageDto getProductImageByImageCode(Long imageCode);


    /**
     * Retrieves a paginated list of products based on the given search criteria.
     *
     * @param criteria the product search criteria
     * @return a paginated list of product data transfer objects
     * @throws InvalidInputException if date input are invalid
     */
    Page<ProductDto> getProductsByCriteria(ProductSearchCriteria criteria);
}
