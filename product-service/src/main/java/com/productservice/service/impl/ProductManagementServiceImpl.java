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
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class ProductManagementServiceImpl implements ProductManagementService {

    @Value("${file.max-size}")
    private long maxFileSize;

    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;
    @PersistenceContext
    private EntityManager entityManager;
    private final ModelMapper modelMapper;


    @Transactional
    @Override
    public ProductCreateRequestDto addProduct(ProductCreateRequestDto request, MultipartFile imageFile) {

        Product existingProduct = productRepository.findByBarcode(request.getBarcode()).orElse(null);
        if (existingProduct != null) {
            if (existingProduct.isDeleted()) {
                throw new ProductAlreadyDeletedException(String.format("Product with barcode %s is deleted. You can re-add it using the appropriate method.", request.getBarcode()));
            } else {
                throw new ProductAlreadyExistsException(String.format("Product with barcode %s already exists.", request.getBarcode()));
            }
        }

        Product product = modelMapper.map(request, Product.class);

        if (imageFile != null && !imageFile.isEmpty()) {
            if (imageFile.getSize() > maxFileSize) {
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
                throw new ImageProcessingException("Failed to process image file");
            }
        }

        product.setCreationDate(LocalDateTime.now());
        productRepository.save(product);


        return modelMapper.map(product, ProductCreateRequestDto.class);
    }



    @Transactional
    @Override
    public UpdateProductRequestDto updateProduct(String barcode, UpdateProductRequestDto updateProductRequestDto, MultipartFile file) {

        if (updateProductRequestDto == null && (file == null || file.isEmpty())){
            throw new InvalidInputException("Both UpdateProductRequestDto and MultipartFile cannot be null or empty");
        }

        if (file != null && !file.isEmpty()){
            if (file.getSize() > maxFileSize) {
                throw new ImageProcessingException("File size cannot exceed 5MB");
            }
        }

        Product product = productRepository.findByBarcodeAndDeletedFalse(barcode)
                .orElseThrow(() -> new ProductNotFoundException(String.format("Product with barcode %s not found", barcode)));

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

        updateProductImage(barcode, updateProductRequestDto, file);

        return updateProductRequestDto;
    }

    @Override
    @Transactional
    public String updateStock(String barcode, int stockChange) {
        Product product = productRepository.findByBarcodeAndDeletedFalse(barcode)
                .orElseThrow(() -> new ProductNotFoundException(String.format("Product with barcode %s not found", barcode)));

        int newStock = product.getStock() + stockChange;
        if (newStock < 0) {
            throw new InvalidInputException(String.format("Insufficient stock for product with barcode %s. Current stock: %s, requested change: %s", barcode, product.getStock(), stockChange));
        }

        product.setStock(newStock);
        product.setLastUpdateDate(LocalDateTime.now());
        productRepository.save(product);

        return String.format("Stock updated for product with barcode %s. New stock level: %d", barcode, newStock);
    }


    @Transactional
    @Override
    public String deleteProductByBarcode(String barcode){

        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ProductNotFoundException(String.format("Product with barcode %s not found", barcode)));

        if (product.isDeleted()){
            throw new ProductAlreadyDeletedException(String.format("Product with barcode %s already deleted", barcode));
        }

        product.setDeleted(true);

        if (product.isHasImage()){
            product.getImage().setDeleted(true);
        }

        productRepository.save(product);
        return barcode;
    }

    @Transactional
    @Override
    public String reAddDeletedProductByBarcode(String barcode) {

        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ProductNotFoundException(String.format("Deleted product with barcode %s not found", barcode)));

        if ( !(product.isDeleted()) ){
            throw new ProductIsNotDeletedException(String.format("Product with barcode %s is not deleted", barcode));}

        product.setDeleted(false);

        if (product.isHasImage()) {
            product.getImage().setDeleted(false);
        }

        productRepository.save(product);
        return barcode;
    }


    @Transactional
    @Override
    public String deleteImageByBarcode(String barcode){

        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ProductNotFoundException(String.format("Product with barcode %s not found", barcode)));
        if (product.isDeleted()){
            throw new ProductAlreadyDeletedException( String.format("Product with barcode %s already deleted. cannot delete the image.", barcode));
        }
        if ( !(product.isHasImage()) || product.getImage().isDeleted() ){
            throw new ImageNotFoundException(String.format("Product with barcode %s does not have an image.", barcode));
        }

        Image image = product.getImage();

        product.setHasImage(false);
        product.setImage(null);
        product.setLastUpdateDate(LocalDateTime.now());
        productRepository.save(product);

        image.setDeleted(true);
        imageRepository.save(image);

        return barcode;
    }


    //--------------------------------------------------------------------------------------------------------------

    public void updateProductImage(String barcode, UpdateProductRequestDto updateProductRequestDto, MultipartFile file){

        if (file != null && !file.isEmpty()) {
            Product product = productRepository.findByBarcodeAndDeletedFalse(barcode)
                    .orElseThrow(() -> new ProductNotFoundException(String.format("Product with barcode %s not found", barcode)));

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
                throw new ImageProcessingException("Failed to process image file");
            }

            product.setLastUpdateDate(LocalDateTime.now());
            productRepository.save(product);
        }
    }



}
