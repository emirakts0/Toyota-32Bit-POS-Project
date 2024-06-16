package com.productservice.service.impl;

import com.productservice.dto.ProductCreateRequestDto;
import com.productservice.dto.UpdateProductRequestDto;
import com.productservice.exception.*;
import com.productservice.model.Image;
import com.productservice.model.Product;
import com.productservice.repository.ImageRepository;
import com.productservice.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductManagementServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MultipartFile imageFile;

    @InjectMocks
    private ProductManagementServiceImpl productManagementService;


    @Test
    void whenAddProductWithValidData_thenProductShouldBeAddedSuccessfully() throws IOException {
        ProductCreateRequestDto requestDto = new ProductCreateRequestDto();
        requestDto.setBarcode("987654321");
        requestDto.setName("Test Product with Image");
        requestDto.setPrice(new BigDecimal(150));
        requestDto.setStock(5);

        when(imageFile.isEmpty()).thenReturn(false);
        when(imageFile.getSize()).thenReturn(0L);
        when(imageFile.getOriginalFilename()).thenReturn("test-image.png");
        when(imageFile.getContentType()).thenReturn("image/png");
        when(imageFile.getBytes()).thenReturn(new byte[]{1, 2, 3});

        Product product = new Product();
        product.setBarcode("987654321");
        product.setName("Test Product with Image");
        product.setPrice(new BigDecimal(150));
        product.setStock(5);
        product.setCreationDate(LocalDateTime.now());
        product.setHasImage(true);

        Image image = new Image("test-image.png", "image/png", new byte[]{1, 2, 3});
        product.setImage(image);

        when(productRepository.findByBarcode(requestDto.getBarcode())).thenReturn(Optional.empty());
        when(modelMapper.map(requestDto, Product.class)).thenReturn(product);
        when(modelMapper.map(product, ProductCreateRequestDto.class)).thenReturn(requestDto);

        ProductCreateRequestDto result = productManagementService.addProduct(requestDto, imageFile);

        assertNotNull(result);
        assertEquals(requestDto.getBarcode(), result.getBarcode());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void whenAddProductWithoutImage_thenProductShouldBeAddedSuccessfully() {
        ProductCreateRequestDto requestDto = new ProductCreateRequestDto();
        requestDto.setBarcode("123456789");
        requestDto.setName("Test Product");
        requestDto.setPrice(new BigDecimal(150));
        requestDto.setStock(10);

        Product product = new Product();
        product.setBarcode("123456789");
        product.setName("Test Product");
        product.setPrice(new BigDecimal(150));
        product.setStock(10);
        product.setCreationDate(LocalDateTime.now());

        when(productRepository.findByBarcode(requestDto.getBarcode())).thenReturn(Optional.empty());
        when(modelMapper.map(requestDto, Product.class)).thenReturn(product);
        when(modelMapper.map(product, ProductCreateRequestDto.class)).thenReturn(requestDto);

        ProductCreateRequestDto result = productManagementService.addProduct(requestDto, null);

        assertNotNull(result);
        assertEquals(requestDto.getBarcode(), result.getBarcode());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void whenAddProductWithExistingBarcode_thenThrowProductAlreadyExistsException() {
        ProductCreateRequestDto requestDto = new ProductCreateRequestDto();
        requestDto.setBarcode("existingBarcode");

        Product existingProduct = new Product();
        when(productRepository.findByBarcode(requestDto.getBarcode())).thenReturn(Optional.of(existingProduct));

        ProductAlreadyExistsException exception = assertThrows(ProductAlreadyExistsException.class,
                () -> productManagementService.addProduct(requestDto, imageFile));

        assertEquals("Product with barcode existingBarcode already exists.", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void whenAddProductWithDeletedBarcode_thenThrowProductAlreadyDeletedException() {
        ProductCreateRequestDto requestDto = new ProductCreateRequestDto();
        requestDto.setBarcode("deletedBarcode");

        Product deletedProduct = new Product();
        deletedProduct.setDeleted(true);
        when(productRepository.findByBarcode(requestDto.getBarcode())).thenReturn(Optional.of(deletedProduct));

        ProductAlreadyDeletedException exception = assertThrows(ProductAlreadyDeletedException.class,
                () -> productManagementService.addProduct(requestDto, imageFile));

        assertEquals("Product with barcode deletedBarcode is deleted. You can re-add it using the appropriate method.", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void whenAddProductWithLargeImageFile_thenThrowImageProcessingException() {
        ProductCreateRequestDto requestDto = new ProductCreateRequestDto();
        requestDto.setBarcode("validBarcode");

        when(productRepository.findByBarcode(requestDto.getBarcode())).thenReturn(Optional.empty());
        when(imageFile.isEmpty()).thenReturn(false);
        when(imageFile.getSize()).thenReturn(1L);

        ImageProcessingException exception = assertThrows(ImageProcessingException.class,
                () -> productManagementService.addProduct(requestDto, imageFile));

        assertEquals("File size cannot exceed 5MB", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void whenAddProductWithImageProcessingError_thenThrowImageProcessingException() throws IOException {
        ProductCreateRequestDto requestDto = new ProductCreateRequestDto();
        requestDto.setBarcode("validBarcode");

        when(productRepository.findByBarcode(requestDto.getBarcode())).thenReturn(Optional.empty());
        when(imageFile.isEmpty()).thenReturn(false);
        when(imageFile.getSize()).thenReturn(0L);
        when(imageFile.getOriginalFilename()).thenReturn("image.jpg");
        when(imageFile.getContentType()).thenReturn("image/jpeg");
        when(imageFile.getBytes()).thenThrow(new IOException("Failed to process image"));

        ImageProcessingException exception = assertThrows(ImageProcessingException.class,
                () -> productManagementService.addProduct(requestDto, imageFile));

        assertEquals("Failed to process image file", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }


    @Test
    void whenUpdateProductWithNullRequestAndFile_thenThrowInvalidInputException() {
        String barcode = "123456789";

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> productManagementService.updateProduct(barcode, null, null));
        assertEquals("Both UpdateProductRequestDto and MultipartFile cannot be null or empty", exception.getMessage());
    }

    @Test
    void whenUpdateProductWithNullRequestAndEmptyFile_thenThrowInvalidInputException() {
        String barcode = "123456789";
        when(imageFile.isEmpty()).thenReturn(true);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> productManagementService.updateProduct(barcode, null, imageFile));

        assertEquals("Both UpdateProductRequestDto and MultipartFile cannot be null or empty", exception.getMessage());
    }

    @Test
    void whenUpdateProductWithNotNullRequestAndEmptyFile_thenThrowImageProcessingException() {
        String barcode = "123456789";
        UpdateProductRequestDto updateProductRequestDto = new UpdateProductRequestDto();
        when(imageFile.isEmpty()).thenReturn(true);

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productManagementService.updateProduct(barcode, updateProductRequestDto, imageFile));

        assertEquals(String.format("Product with barcode %s not found", barcode), exception.getMessage());
    }

    @Test
    void whenUpdateProductWithTooLargeFile_thenThrowImageProcessingException() {
        String barcode = "123456789";
        when(imageFile.isEmpty()).thenReturn(false);
        when(imageFile.getSize()).thenReturn(10L * 1024L * 1024L); // 10MB

        ImageProcessingException exception = assertThrows(ImageProcessingException.class,
                () -> productManagementService.updateProduct(barcode, null, imageFile));
        assertEquals("File size cannot exceed 5MB", exception.getMessage());
    }

    @Test
    void whenUpdateProductWithoutImage_thenUpdateProductDetailsSuccessfully() {
        String barcode = "123456789";
        UpdateProductRequestDto updateProductRequestDto = new UpdateProductRequestDto();
        updateProductRequestDto.setName("Updated Product");
        updateProductRequestDto.setPrice(new BigDecimal(150));
        updateProductRequestDto.setStock(20);

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Original Product");
        product.setPrice(new BigDecimal(100));
        product.setStock(10);

        when(productRepository.findByBarcodeAndDeletedFalse(barcode)).thenReturn(Optional.of(product));

        UpdateProductRequestDto result = productManagementService.updateProduct(barcode, updateProductRequestDto, null);

        verify(productRepository, times(1)).updateProductByBarcode(
                eq(barcode),
                eq("Updated Product"),
                eq(new BigDecimal(150)),
                eq(20),
                any(LocalDateTime.class)
        );

        verify(entityManager, times(1)).refresh(product);

        assertEquals(updateProductRequestDto, result);
    }

    @Test
    void whenUpdateProductWithExistingImageAndNewImage_thenUpdateProductAndImageSuccessfully() throws IOException {
        String barcode = "123456789";
        UpdateProductRequestDto updateProductRequestDto = new UpdateProductRequestDto();
        updateProductRequestDto.setName("Updated Product");
        updateProductRequestDto.setPrice(new BigDecimal(150));
        updateProductRequestDto.setStock(20);

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Original Product");
        product.setPrice(new BigDecimal(100));
        product.setStock(10);

        when(imageFile.isEmpty()).thenReturn(false);
        when(imageFile.getOriginalFilename()).thenReturn("new-image.png");
        when(imageFile.getContentType()).thenReturn("image/png");
        when(imageFile.getBytes()).thenReturn(new byte[]{1, 2, 3});

        Image existingImage = new Image();
        product.setImage(existingImage);

        when(productRepository.findByBarcodeAndDeletedFalse(barcode)).thenReturn(Optional.of(product));

        UpdateProductRequestDto result = productManagementService.updateProduct(barcode, updateProductRequestDto, imageFile);

        verify(productRepository, times(1)).updateProductByBarcode(
                eq(barcode),
                eq("Updated Product"),
                eq(new BigDecimal(150)),
                eq(20),
                any(LocalDateTime.class)
        );
        verify(entityManager, times(1)).refresh(product);

        verify(imageRepository, times(1)).save(existingImage);
        verify(productRepository, times(1)).save(product);

        assertEquals(updateProductRequestDto, result);
        assertEquals("new-image.png", product.getImage().getFileName());
        assertEquals("image/png", product.getImage().getType());
        assertArrayEquals(new byte[]{1, 2, 3}, product.getImage().getFile());
    }

    @Test
    void whenUpdateProductImageAndFailToProcessImage_thenThrowImageProcessingException() throws IOException {
        String barcode = "123456789";
        when(imageFile.isEmpty()).thenReturn(false);
        when(imageFile.getOriginalFilename()).thenReturn("new-image.png");
        when(imageFile.getContentType()).thenReturn("image/png");
        when(imageFile.getBytes()).thenThrow(new IOException("Failed to process image"));

        Product product = new Product();
        product.setBarcode(barcode);

        when(productRepository.findByBarcodeAndDeletedFalse(barcode)).thenReturn(Optional.of(product));

        ImageProcessingException exception = assertThrows(ImageProcessingException.class, () ->
                productManagementService.updateProduct(barcode, null, imageFile));
        assertEquals("Failed to process image file", exception.getMessage());
    }


    @Test
    void whenUpdateStockWithValidStockChange_thenStockUpdatedSuccessfully() {
        String barcode = "123456789";
        int stockChange = 5;

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Test Product");
        product.setPrice(new BigDecimal(100));
        product.setStock(10);

        when(productRepository.findByBarcodeAndDeletedFalse(barcode)).thenReturn(Optional.of(product));

        String result = productManagementService.updateStock(barcode, stockChange);

        verify(productRepository, times(1)).findByBarcodeAndDeletedFalse(barcode);
        verify(productRepository, times(1)).save(product);

        assertEquals(15, product.getStock());
        assertNotNull(product.getLastUpdateDate());
        assertEquals(String.format("Stock updated for product with barcode %s. New stock level: %d", barcode, 15), result);
    }

    @Test
    void whenUpdateStockWithNegativeStockChange_thenStockUpdatedSuccessfully() {
        String barcode = "123456789";
        int stockChange = -5;

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Test Product");
        product.setPrice(new BigDecimal(100));
        product.setStock(10);

        when(productRepository.findByBarcodeAndDeletedFalse(barcode)).thenReturn(Optional.of(product));

        String result = productManagementService.updateStock(barcode, stockChange);

        verify(productRepository, times(1)).findByBarcodeAndDeletedFalse(barcode);
        verify(productRepository, times(1)).save(product);

        assertEquals(5, product.getStock());
        assertNotNull(product.getLastUpdateDate());
        assertEquals(String.format("Stock updated for product with barcode %s. New stock level: %d", barcode, 5), result);
    }

    @Test
    void whenUpdateStockWithInsufficientStock_thenThrowInvalidInputException() {
        String barcode = "123456789";
        int stockChange = -15;

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Test Product");
        product.setPrice(new BigDecimal(100));
        product.setStock(10);

        when(productRepository.findByBarcodeAndDeletedFalse(barcode)).thenReturn(Optional.of(product));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> productManagementService.updateStock(barcode, stockChange));

        verify(productRepository, times(1)).findByBarcodeAndDeletedFalse(barcode);
        verify(productRepository, never()).save(any(Product.class));

        assertEquals(String.format("Insufficient stock for product with barcode %s. Current stock: %s, requested change: %s",
                barcode, 10, stockChange), exception.getMessage());
    }

    @Test
    void whenUpdateStockWithNonExistingProduct_thenThrowProductNotFoundException() {
        String barcode = "123456789";
        int stockChange = 5;

        when(productRepository.findByBarcodeAndDeletedFalse(barcode)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productManagementService.updateStock(barcode, stockChange));

        verify(productRepository, times(1)).findByBarcodeAndDeletedFalse(barcode);
        verify(productRepository, never()).save(any(Product.class));

        assertEquals(String.format("Product with barcode %s not found", barcode), exception.getMessage());
    }


    @Test
    void whenDeleteProductByBarcodeWithExistingProduct_thenProductDeletedSuccessfully() {
        String barcode = "123456789";

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Test Product");
        product.setPrice(new BigDecimal(100));
        product.setStock(10);
        product.setDeleted(false);

        when(productRepository.findByBarcode(barcode)).thenReturn(Optional.of(product));

        String result = productManagementService.deleteProductByBarcode(barcode);

        verify(productRepository, times(1)).findByBarcode(barcode);
        verify(productRepository, times(1)).save(product);

        assertTrue(product.isDeleted());
        assertEquals(barcode, result);
    }

    @Test
    void whenDeleteProductByBarcodeWithNonExistingProduct_thenThrowProductNotFoundException() {
        String barcode = "123456789";

        when(productRepository.findByBarcode(barcode)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productManagementService.deleteProductByBarcode(barcode));

        verify(productRepository, times(1)).findByBarcode(barcode);
        verify(productRepository, never()).save(any(Product.class));

        assertEquals(String.format("Product with barcode %s not found", barcode), exception.getMessage());
    }

    @Test
    void whenDeleteProductByBarcodeWithAlreadyDeletedProduct_thenThrowProductAlreadyDeletedException() {
        String barcode = "123456789";

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Test Product");
        product.setPrice(new BigDecimal(100));
        product.setStock(10);
        product.setDeleted(true);

        when(productRepository.findByBarcode(barcode)).thenReturn(Optional.of(product));

        ProductAlreadyDeletedException exception = assertThrows(ProductAlreadyDeletedException.class,
                () -> productManagementService.deleteProductByBarcode(barcode));

        verify(productRepository, times(1)).findByBarcode(barcode);
        verify(productRepository, never()).save(any(Product.class));

        assertEquals(String.format("Product with barcode %s already deleted", barcode), exception.getMessage());
    }

    @Test
    void whenDeleteProductByBarcodeWithImage_thenImageDeletedSuccessfully() {
        String barcode = "123456789";

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Test Product");
        product.setPrice(new BigDecimal(100));
        product.setStock(10);
        product.setDeleted(false);
        Image image = new Image();
        product.setImage(image);
        product.setHasImage(true);

        when(productRepository.findByBarcode(barcode)).thenReturn(Optional.of(product));

        String result = productManagementService.deleteProductByBarcode(barcode);

        verify(productRepository, times(1)).findByBarcode(barcode);
        verify(productRepository, times(1)).save(product);

        assertTrue(product.isDeleted());
        assertTrue(product.getImage().isDeleted());
        assertEquals(barcode, result);
    }


    @Test
    void whenReAddDeletedProductByBarcodeWithExistingDeletedProduct_thenProductReAddedSuccessfully() {
        String barcode = "123456789";

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Test Product");
        product.setPrice(new BigDecimal(100));
        product.setStock(10);
        product.setDeleted(true);

        when(productRepository.findByBarcode(barcode)).thenReturn(Optional.of(product));

        String result = productManagementService.reAddDeletedProductByBarcode(barcode);

        verify(productRepository, times(1)).findByBarcode(barcode);
        verify(productRepository, times(1)).save(product);

        assertFalse(product.isDeleted());
        assertEquals(barcode, result);
    }

    @Test
    void whenReAddDeletedProductByBarcodeWithNonExistingProduct_thenThrowProductNotFoundException() {
        String barcode = "123456789";

        when(productRepository.findByBarcode(barcode)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productManagementService.reAddDeletedProductByBarcode(barcode));

        verify(productRepository, times(1)).findByBarcode(barcode);
        verify(productRepository, never()).save(any(Product.class));

        assertEquals(String.format("Deleted product with barcode %s not found", barcode), exception.getMessage());
    }

    @Test
    void whenReAddDeletedProductByBarcodeWithNotDeletedProduct_thenThrowProductIsNotDeletedException() {
        String barcode = "123456789";

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Test Product");
        product.setPrice(new BigDecimal(100));
        product.setStock(10);
        product.setDeleted(false);

        when(productRepository.findByBarcode(barcode)).thenReturn(Optional.of(product));

        ProductIsNotDeletedException exception = assertThrows(ProductIsNotDeletedException.class,
                () -> productManagementService.reAddDeletedProductByBarcode(barcode));

        verify(productRepository, times(1)).findByBarcode(barcode);
        verify(productRepository, never()).save(any(Product.class));

        assertEquals(String.format("Product with barcode %s is not deleted", barcode), exception.getMessage());
    }

    @Test
    void whenReAddDeletedProductByBarcodeWithImage_thenImageReAddedSuccessfully() {
        String barcode = "123456789";

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Test Product");
        product.setPrice(new BigDecimal(100));
        product.setStock(10);
        product.setDeleted(true);
        Image image = new Image();
        image.setDeleted(true);
        product.setImage(image);
        product.setHasImage(true);

        when(productRepository.findByBarcode(barcode)).thenReturn(Optional.of(product));

        String result = productManagementService.reAddDeletedProductByBarcode(barcode);

        verify(productRepository, times(1)).findByBarcode(barcode);
        verify(productRepository, times(1)).save(product);

        assertFalse(product.isDeleted());
        assertFalse(product.getImage().isDeleted());
        assertEquals(barcode, result);
    }


    @Test
    void whenDeleteImageByBarcodeWithExistingProductAndImage_thenImageDeletedSuccessfully() {
        String barcode = "123456789";

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Test Product");
        product.setPrice(new BigDecimal(100));
        product.setStock(10);
        product.setDeleted(false);
        Image image = new Image();
        image.setDeleted(false);
        product.setImage(image);
        product.setHasImage(true);

        when(productRepository.findByBarcode(barcode)).thenReturn(Optional.of(product));

        String result = productManagementService.deleteImageByBarcode(barcode);

        verify(productRepository, times(1)).findByBarcode(barcode);
        verify(productRepository, times(1)).save(product);
        verify(imageRepository, times(1)).save(image);

        assertFalse(product.isHasImage());
        assertNull(product.getImage());
        assertTrue(image.isDeleted());
        assertEquals(barcode, result);
    }

    @Test
    void whenDeleteImageByBarcodeWithNonExistingProduct_thenThrowProductNotFoundException() {
        String barcode = "123456789";

        when(productRepository.findByBarcode(barcode)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productManagementService.deleteImageByBarcode(barcode));

        verify(productRepository, times(1)).findByBarcode(barcode);
        verify(productRepository, never()).save(any(Product.class));
        verify(imageRepository, never()).save(any(Image.class));

        assertEquals(String.format("Product with barcode %s not found", barcode), exception.getMessage());
    }

    @Test
    void whenDeleteImageByBarcodeWithAlreadyDeletedProduct_thenThrowProductAlreadyDeletedException() {
        String barcode = "123456789";

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Test Product");
        product.setPrice(new BigDecimal(100));
        product.setStock(10);
        product.setDeleted(true);

        when(productRepository.findByBarcode(barcode)).thenReturn(Optional.of(product));

        ProductAlreadyDeletedException exception = assertThrows(ProductAlreadyDeletedException.class,
                () -> productManagementService.deleteImageByBarcode(barcode));

        verify(productRepository, times(1)).findByBarcode(barcode);
        verify(productRepository, never()).save(any(Product.class));
        verify(imageRepository, never()).save(any(Image.class));

        assertEquals(String.format("Product with barcode %s already deleted. cannot delete the image.", barcode), exception.getMessage());
    }

    @Test
    void whenDeleteImageByBarcodeWithNoImage_thenThrowImageNotFoundException() {
        String barcode = "123456789";

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Test Product");
        product.setPrice(new BigDecimal(100));
        product.setStock(10);
        product.setDeleted(false);
        product.setHasImage(false);
        product.setImage(null);

        when(productRepository.findByBarcode(barcode)).thenReturn(Optional.of(product));

        ImageNotFoundException exception = assertThrows(ImageNotFoundException.class,
                () -> productManagementService.deleteImageByBarcode(barcode));

        verify(productRepository, times(1)).findByBarcode(barcode);
        verify(productRepository, never()).save(any(Product.class));
        verify(imageRepository, never()).save(any(Image.class));

        assertEquals(String.format("Product with barcode %s does not have an image.", barcode), exception.getMessage());
    }

    @Test
    void whenDeleteImageByBarcodeWithAlreadyDeletedImage_thenThrowImageNotFoundException() {
        String barcode = "123456789";

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName("Test Product");
        product.setPrice(new BigDecimal(100));
        product.setStock(10);
        product.setDeleted(false);
        Image image = new Image();
        image.setDeleted(true);
        product.setImage(image);
        product.setHasImage(true);

        when(productRepository.findByBarcode(barcode)).thenReturn(Optional.of(product));

        ImageNotFoundException exception = assertThrows(ImageNotFoundException.class,
                () -> productManagementService.deleteImageByBarcode(barcode));

        verify(productRepository, times(1)).findByBarcode(barcode);
        verify(productRepository, never()).save(any(Product.class));
        verify(imageRepository, never()).save(any(Image.class));

        assertEquals(String.format("Product with barcode %s does not have an image.", barcode), exception.getMessage());
    }
}