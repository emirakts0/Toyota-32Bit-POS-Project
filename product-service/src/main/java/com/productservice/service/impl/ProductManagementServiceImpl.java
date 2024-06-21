package com.productservice.service.impl;

import com.productservice.dto.ProductCreateRequestDto;
import com.productservice.dto.UpdateProductRequestDto;
import com.productservice.exception.*;
import com.productservice.model.Image;
import com.productservice.model.Product;
import com.productservice.repository.ImageRepository;
import com.productservice.repository.ProductRepository;
import com.productservice.service.ProductManagementService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProductManagementServiceImpl implements ProductManagementService {

    @Value("${file.max-size}")
    private long maxFileSize;

    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;
    private final EntityManager entityManager;
    private final ModelMapper modelMapper;


    @Transactional
    @Override
    public ProductCreateRequestDto addProduct(ProductCreateRequestDto request, MultipartFile imageFile) {
        log.trace("addProduct method begins. Request: {}", request);

        Product existingProduct = productRepository.findByBarcode(request.getBarcode()).orElse(null);
        if (existingProduct != null) {
            if (existingProduct.isDeleted()) {
                log.warn("addProduct: Product with barcode {} is deleted", request.getBarcode());
                throw new ProductAlreadyDeletedException(String.format("Product with barcode %s is deleted. You can re-add it using the appropriate method.", request.getBarcode()));
            } else {
                log.warn("addProduct: Product with barcode {} already exists", request.getBarcode());
                throw new ProductAlreadyExistsException(String.format("Product with barcode %s already exists.", request.getBarcode()));
            }
        }

        Product product = modelMapper.map(request, Product.class);

        if (imageFile != null && !imageFile.isEmpty()) {
            if (imageFile.getSize() > maxFileSize) {
                log.warn("addProduct: Image file size exceeds limit of {} bytes", maxFileSize);
                throw new ImageProcessingException("File size cannot exceed 5MB");
            }

            try {
                Image image = new Image(
                        imageFile.getOriginalFilename(),
                        imageFile.getContentType(),
                        imageFile.getBytes()
                );

                product.setImage(image);
                product.setHasImage(true);
            } catch (IOException e) {
                log.warn("addProduct: Failed to process image file", e);
                throw new ImageProcessingException("Failed to process image file");
            }
        }

        product.setCreationDate(LocalDateTime.now());
        productRepository.save(product);
        log.info("addProduct: Product added successfully with barcode {}", product.getBarcode());

        log.trace("addProduct method ends. Request: {}", request);
        return modelMapper.map(product, ProductCreateRequestDto.class);
    }


    @Transactional
    @Override
    public UpdateProductRequestDto updateProduct(String barcode, UpdateProductRequestDto updateProductRequestDto, MultipartFile file) {
        log.trace("updateProduct method begins. Barcode: {}, Request: {}", barcode, updateProductRequestDto);

        if (updateProductRequestDto == null && (file == null || file.isEmpty())){
            log.warn("updateProduct: Both UpdateProductRequestDto and MultipartFile cannot be null or empty");
            throw new InvalidInputException("Both UpdateProductRequestDto and MultipartFile cannot be null or empty");
        }

        if (file != null && !file.isEmpty()){
            if (file.getSize() > maxFileSize) {
                log.warn("updateProduct: Image file size exceeds limit of {} bytes", maxFileSize);
                throw new ImageProcessingException("File size cannot exceed 5MB");
            }
        }

        Product product = productRepository.findByBarcodeAndDeletedFalse(barcode)
                .orElseThrow(() -> {
                    log.warn("updateProduct: Product with barcode {} not found", barcode);
                    return new ProductNotFoundException(String.format("Product with barcode %s not found", barcode)); });

        if(updateProductRequestDto != null){

            productRepository.updateProductByBarcode(
                    barcode,
                    updateProductRequestDto.getName(),
                    updateProductRequestDto.getPrice(),
                    updateProductRequestDto.getStock(),
                    LocalDateTime.now()
            );
            entityManager.refresh(product);
        }

        updateProductImage(product, file);
        log.info("updateProduct: Product updated successfully with barcode {}", barcode);

        log.trace("updateProduct method ends. Barcode: {}, Request: {}", barcode, updateProductRequestDto);
        return updateProductRequestDto;
    }


    @Override
    @Transactional
    public String updateStock(String barcode, int stockChange) {
        log.trace("updateStock method begins. Barcode: {}, StockChange: {}", barcode, stockChange);

        Product product = productRepository.findByBarcodeAndDeletedFalse(barcode)
                .orElseThrow(() -> {
                    log.warn("updateStock: Product with barcode {} not found", barcode);
                    return new ProductNotFoundException(String.format("Product with barcode %s not found", barcode)); });

        int newStock = product.getStock() + stockChange;
        if (newStock < 0) {
            log.warn("updateStock: Insufficient stock for product with barcode {}. Current stock: {}, Requested change: {}",
                    barcode, product.getStock(), stockChange);
            throw new InvalidInputException(String.format("Insufficient stock for product with barcode %s. Current stock: %s, requested change: %s",
                    barcode, product.getStock(), stockChange));
        }

        product.setStock(newStock);
        product.setLastUpdateDate(LocalDateTime.now());
        productRepository.save(product);
        log.info("updateStock: Stock updated for product with barcode {}. New stock level: {}", barcode, newStock);

        log.trace("updateStock method ends. Barcode: {}, StockChange: {}", barcode, stockChange);
        return String.format("Stock updated for product with barcode %s. New stock level: %d", barcode, newStock);
    }


    @Transactional
    @Override
    public String deleteProductByBarcode(String barcode){
        log.trace("deleteProductByBarcode method begins. Barcode: {}", barcode);

        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> {
                    log.warn("deleteProductByBarcode: Product with barcode {} not found", barcode);
                    return new ProductNotFoundException(String.format("Product with barcode %s not found", barcode)); });

        if (product.isDeleted()) {
            log.warn("deleteProductByBarcode: Product with barcode {} already deleted", barcode);
            throw new ProductAlreadyDeletedException(String.format("Product with barcode %s already deleted", barcode));
        }

        product.setDeleted(true);

        if (product.isHasImage()){
            product.getImage().setDeleted(true);
        }

        productRepository.save(product);
        log.info("deleteProductByBarcode: Product deleted successfully with barcode {}", barcode);

        log.trace("deleteProductByBarcode method ends. Barcode: {}", barcode);
        return barcode;
    }


    @Transactional
    @Override
    public String reAddDeletedProductByBarcode(String barcode) {
        log.trace("reAddDeletedProductByBarcode method begins. Barcode: {}", barcode);

        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> {
                    log.warn("reAddDeletedProductByBarcode: Deleted product with barcode {} not found", barcode);
                    return new ProductNotFoundException(String.format("Deleted product with barcode %s not found", barcode)); });

        if (!product.isDeleted()) {
            log.warn("reAddDeletedProductByBarcode: Product with barcode {} is not deleted", barcode);
            throw new ProductIsNotDeletedException(String.format("Product with barcode %s is not deleted", barcode));
        }

        product.setDeleted(false);

        if (product.isHasImage()) {
            product.getImage().setDeleted(false);
        }

        productRepository.save(product);
        log.info("reAddDeletedProductByBarcode: Product re-added successfully with barcode {}", barcode);

        log.trace("reAddDeletedProductByBarcode method ends. Barcode: {}", barcode);
        return barcode;
    }


    @Transactional
    @Override
    public String deleteImageByBarcode(String barcode){
        log.trace("deleteImageByBarcode method begins. Barcode: {}", barcode);

        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> {
                    log.warn("deleteImageByBarcode: Product with barcode {} not found", barcode);
                    return new ProductNotFoundException(String.format("Product with barcode %s not found", barcode)); });

        if (product.isDeleted()) {
            log.warn("deleteImageByBarcode: Product with barcode {} already deleted, cannot delete the image", barcode);
            throw new ProductAlreadyDeletedException(String.format("Product with barcode %s already deleted. cannot delete the image.", barcode));
        }

        if (!product.isHasImage() || product.getImage().isDeleted()) {
            log.warn("deleteImageByBarcode: Product with barcode {} does not have an image", barcode);
            throw new ImageNotFoundException(String.format("Product with barcode %s does not have an image.", barcode));
        }

        Image image = product.getImage();

        product.setHasImage(false);
        product.setImage(null);
        product.setLastUpdateDate(LocalDateTime.now());
        productRepository.save(product);

        image.setDeleted(true);
        imageRepository.save(image);
        log.info("deleteImageByBarcode: Image deleted for product with barcode {}", barcode);

        log.trace("deleteImageByBarcode method ends. Barcode: {}", barcode);
        return barcode;
    }



    private void updateProductImage(Product product, MultipartFile file){
        log.trace("updateProductImage method begins. product: {}", product);

        if (file != null && !file.isEmpty()) {
            if (product.getImage() != null) {

                Image image = product.getImage();
                image.setDeleted(true);
                imageRepository.save(image);
            }

            try {
                Image newImage = new Image(
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getBytes()
                );

                product.setImage(newImage);
                product.setHasImage(true);
            } catch (IOException e) {
                log.warn("updateProductImage: Failed to process image file", e);
                throw new ImageProcessingException("Failed to process image file");
            }

            product.setLastUpdateDate(LocalDateTime.now());
            productRepository.save(product);
            log.info("updateProductImage: Image updated for product with barcode {}", product.getBarcode());
        }
        log.trace("updateProductImage method ends. product: {}", product);
    }
}
