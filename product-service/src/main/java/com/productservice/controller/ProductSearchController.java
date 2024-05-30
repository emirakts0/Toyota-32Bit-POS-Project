package com.productservice.controller;

import com.productservice.dto.ImageDto;
import com.productservice.dto.ProductDto;
import com.productservice.dto.ProductSearchCriteria;
import com.productservice.service.ProductSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@Validated
@Slf4j
@RequestMapping("/product/search")
public class ProductSearchController {

    private final ProductSearchService productSearchService;


    @GetMapping("/{barcode}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable String barcode) {
        log.trace("getProduct endpoint called for barcode: {}", barcode);

        ProductDto productDto = productSearchService.getProductByBarcode(barcode);
        return ResponseEntity.ok().body(productDto);
    }


    @GetMapping("/by-prefix")
    public ResponseEntity<Page<ProductDto>> getProductsByPrefix(
            @RequestParam String prefix,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "true") boolean hideDeleted) {
        log.trace("getProductsByPrefix endpoint called with prefix: {}, pageSize: {}, pageNumber: {}, hideDeleted: {}",
                prefix, pageSize, pageNumber, hideDeleted);

        Page<ProductDto> productsPage = productSearchService.getProductsByPrefix(prefix,
                pageSize,
                pageNumber,
                hideDeleted);
        return ResponseEntity.ok(productsPage);
    }


    @GetMapping("/filter")
    public ResponseEntity<Page<ProductDto>> getProductsByFilterAndPagination(@RequestBody
                                                                             @Valid ProductSearchCriteria productSearchCriteria){
        log.trace("getProductsByFilterAndPagination endpoint called with search criteria: {}", productSearchCriteria);

        Page<ProductDto> productPage = productSearchService.getProductsByCriteria(productSearchCriteria);
        return ResponseEntity.ok(productPage);
    }


    @GetMapping("/image/{imageCode}")
    public ResponseEntity<byte[]> getImageByImageCode(@PathVariable Long imageCode) {
        log.trace("getImageByImageCode endpoint called for imageCode: {}", imageCode);

        ImageDto imageDto = productSearchService.getProductImageByImageCode(imageCode);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(imageDto.getType()))
                .header("Content-Disposition", "attachment; filename=\"" + imageDto.getFileName() + "\"")
                .body(imageDto.getFile());
    }
}
