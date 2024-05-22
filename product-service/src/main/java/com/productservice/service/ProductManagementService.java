package com.productservice.service;

import com.productservice.dto.ProductCreateRequestDto;
import com.productservice.dto.UpdateProductRequestDto;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;


public interface ProductManagementService {

    @Transactional
    ProductCreateRequestDto addProduct(ProductCreateRequestDto request,
                                       MultipartFile imageFile);

    @Transactional
    UpdateProductRequestDto updateProduct(String barcode,
                                          UpdateProductRequestDto updateProductRequestDto,
                                          MultipartFile file);

    @Transactional
    String deleteProductByBarcode(String barcode);

    @Transactional
    String deleteImageByBarcode(String barcode);

    @Transactional
    String reAddDeletedProductByBarcode(String barcode);

    @Transactional
    String updateStock(String Barcode, int stock);
    }
