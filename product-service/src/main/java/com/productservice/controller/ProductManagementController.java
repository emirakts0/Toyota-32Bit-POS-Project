package com.productservice.controller;

import com.productservice.service.ProductManagementService;
import com.productservice.dto.ProductCreateRequestDto;
import com.productservice.dto.UpdateProductRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@Validated
@Slf4j
@RequestMapping("/product/management")
public class ProductManagementController {

    private final ProductManagementService productManagementService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> addProduct(@RequestPart(value = "file", required = false) MultipartFile file,
                                             @Valid @RequestPart(value = "product") ProductCreateRequestDto productCreateRequestDto){
        log.trace("addProduct endpoint called with product data: {}", productCreateRequestDto);

        productManagementService.addProduct(productCreateRequestDto, file);
        return ResponseEntity.status(HttpStatus.CREATED).body("Product saved successfully");
    }


    @PutMapping(path = "/{barcode}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateProduct(@RequestPart(value = "file", required = false) MultipartFile file,
                                                @Valid @RequestPart(value = "product", required = false) UpdateProductRequestDto updateProductRequestDto,
                                                @PathVariable String barcode){
        log.trace("updateProduct endpoint called for barcode: {}", barcode);

        productManagementService.updateProduct(barcode, updateProductRequestDto,file);
        return ResponseEntity.ok().body( "Product updated successfully" );
    }


    @PutMapping("/{barcode}/stock")
    public ResponseEntity<String> updateStock(@PathVariable String barcode,
                                              @RequestParam int stockChange) {
        log.trace("updateStock endpoint called for barcode: {} with stock change: {}", barcode, stockChange);

        String response = productManagementService.updateStock(barcode, stockChange);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{barcode}")
    public ResponseEntity<String> deleteProduct(@PathVariable String barcode){
        log.trace("deleteProduct endpoint called for barcode: {}", barcode);

        productManagementService.deleteProductByBarcode(barcode);
        return ResponseEntity.ok().body(String.format("The product with the barcode %s has been deleted.", barcode));
    }


    @PutMapping("/re-add/{barcode}")
    public ResponseEntity<String> reAddDeletedProduct(@PathVariable String barcode) {
        log.trace("reAddDeletedProduct endpoint called for barcode: {}", barcode);

        productManagementService.reAddDeletedProductByBarcode(barcode);
        return ResponseEntity.ok().body(String.format("The product with the barcode %s has been re-added.", barcode));
    }


    @DeleteMapping("/delete-image/{barcode}")
    public ResponseEntity<String> deleteProductImage(@PathVariable String barcode){
        log.trace("deleteProductImage endpoint called for barcode: {}", barcode);

        productManagementService.deleteImageByBarcode(barcode);
        return ResponseEntity.ok().body(String.format("The product image with the barcode %s has been deleted.", barcode));
    }
}
