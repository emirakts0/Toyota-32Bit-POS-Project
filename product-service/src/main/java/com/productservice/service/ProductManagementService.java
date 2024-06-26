package com.productservice.service;

import com.productservice.dto.ProductCreateRequestDto;
import com.productservice.dto.UpdateProductRequestDto;
import com.productservice.exception.*;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for managing products.
 * Provides methods for adding, updating, deleting, and managing stock of products.
 * @author Emir Aktaş
 */
public interface ProductManagementService {

    /**
     * Adds a new product along with its image.
     *
     * @param  request   the product creation request data transfer object
     * @param  imageFile the image file to be associated with the product
     * @return the created product's data transfer object
     * @throws ProductAlreadyExistsException if a product with the same barcode already exists and is not deleted
     * @throws ProductAlreadyDeletedException if a product with the same barcode already exists and is marked as deleted
     * @throws ImageProcessingException if there is an error processing the image file
     */
    @Transactional
    ProductCreateRequestDto addProduct(ProductCreateRequestDto request,
                                       MultipartFile imageFile);


    /**
     * Updates an existing product's details and/or image.
     *
     * @param  barcode                  the barcode of the product to be updated
     * @param  updateProductRequestDto  the product update request data transfer object
     * @param  file                     the new image file to be associated with the product, if any
     * @return the updated product's data transfer object
     * @throws InvalidInputException if both updateProductRequestDto and MultipartFile are null or empty, or if the image file size exceeds the limit
     * @throws ProductNotFoundException if the product with the given barcode is not found
     * @throws ImageProcessingException if there is an error processing the image file
     */
    @Transactional
    UpdateProductRequestDto updateProduct(String barcode,
                                          UpdateProductRequestDto updateProductRequestDto,
                                          MultipartFile file);


    /**
     * Updates the stock level of a product by its barcode.
     *
     * @param  barcode the barcode of the product whose stock is to be updated
     * @param  stock the amount by which the stock is to be changed (can be positive or negative)
     * @return a message indicating the new stock level of the product
     * @throws ProductNotFoundException if the product with the given barcode is not found
     * @throws InvalidInputException if the resulting stock level is negative
     */
    @Transactional
    String updateStock(String barcode, int stock);


    /**
     * Deletes a product by its barcode.
     *
     * @param  barcode the barcode of the product to be deleted
     * @return the barcode of the deleted product
     * @throws ProductNotFoundException if the product with the given barcode is not found
     * @throws ProductAlreadyDeletedException if the product with the given barcode is already deleted
     */
    @Transactional
    String deleteProductByBarcode(String barcode);


    /**
     * Deletes the image associated with a product by its barcode.
     *
     * @param  barcode the barcode of the product whose image is to be deleted
     * @return the barcode of the product whose image was deleted
     * @throws ProductNotFoundException if the product with the given barcode is not found
     * @throws ProductAlreadyDeletedException if the product with the given barcode is already deleted
     * @throws ImageNotFoundException if the product does not have an image or the image is already deleted
     */
    @Transactional
    String deleteImageByBarcode(String barcode);


    /**
     * Re-adds a previously deleted product by its barcode.
     *
     * @param  barcode the barcode of the product to be re-added
     * @return the barcode of the re-added product
     * @throws ProductNotFoundException if the product with the given barcode is not found
     * @throws ProductIsNotDeletedException if the product with the given barcode is not marked as deleted
     */
    @Transactional
    String reAddDeletedProductByBarcode(String barcode);
}
