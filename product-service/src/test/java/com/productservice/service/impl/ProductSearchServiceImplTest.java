package com.productservice.service.impl;

import com.productservice.dto.ImageDto;
import com.productservice.dto.ProductDto;
import com.productservice.dto.ProductSearchCriteria;
import com.productservice.exception.ImageNotFoundException;
import com.productservice.exception.InvalidInputException;
import com.productservice.exception.ProductNotFoundException;
import com.productservice.model.Image;
import com.productservice.model.Product;
import com.productservice.repository.ImageRepository;
import com.productservice.repository.ProductRepository;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductSearchServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    Root<Product> root;

    @Mock
    CriteriaQuery<?> query;

    @Mock
    CriteriaBuilder criteriaBuilder;

    @Mock
    Predicate predicate;

    @InjectMocks
    private ProductSearchServiceImpl productSearchService;


    @Test
    void whenGetProductByBarcodeWithValidBarcode_thenReturnProductDto() {
        String barcode = "validBarcode";
        Product product = new Product();
        product.setBarcode(barcode);
        ProductDto productDto = new ProductDto();
        productDto.setBarcode(barcode);

        when(productRepository.findByBarcodeAndDeletedFalse(barcode)).thenReturn(Optional.of(product));
        when(modelMapper.map(product, ProductDto.class)).thenReturn(productDto);

        ProductDto result = productSearchService.getProductByBarcode(barcode);

        assertNotNull(result);
        assertEquals(barcode, result.getBarcode());
        verify(productRepository, times(1)).findByBarcodeAndDeletedFalse(barcode);
        verify(modelMapper, times(1)).map(product, ProductDto.class);
    }

    @Test
    void whenGetProductByBarcodeWithInvalidBarcode_thenThrowProductNotFoundException() {
        String barcode = "invalidBarcode";

        when(productRepository.findByBarcodeAndDeletedFalse(barcode)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class, () -> productSearchService.getProductByBarcode(barcode));

        assertEquals("Product with barcode invalidBarcode not found", exception.getMessage());
        verify(productRepository, times(1)).findByBarcodeAndDeletedFalse(barcode);
        verify(modelMapper, never()).map(any(Product.class), any(ProductDto.class));
    }

    @Test
    void whenGetProductByBarcodeWithValidBarcodeAndImage_thenReturnProductDtoWithImageCode() {
        String barcode = "validBarcode";
        Product product = new Product();
        product.setBarcode(barcode);
        Image image = new Image();
        image.setImageCode(123L);
        product.setImage(image);
        ProductDto productDto = new ProductDto();
        productDto.setBarcode(barcode);

        when(productRepository.findByBarcodeAndDeletedFalse(barcode)).thenReturn(Optional.of(product));
        when(modelMapper.map(product, ProductDto.class)).thenReturn(productDto);

        ProductDto result = productSearchService.getProductByBarcode(barcode);

        assertNotNull(result);
        assertEquals(barcode, result.getBarcode());
        assertEquals(123L, result.getImageCode());
        verify(productRepository, times(1)).findByBarcodeAndDeletedFalse(barcode);
        verify(modelMapper, times(1)).map(product, ProductDto.class);
    }


    @Test
    void whenGetProductsByPrefixWithValidParamsAndHideDeletedTrue_thenReturnNonDeletedProducts() {
        String prefix = "validPrefix";
        int pageSize = 1;
        int pageNumber = 1;
        boolean hideDeleted = true;

        Product product1 = createProduct("validPrefixProduct1", false, null);
        Product product2 = createProduct("validPrefixProduct2", false, 124L);
        List<Product> products = Arrays.asList(product1, product2);
        Page<Product> productPage = new PageImpl<>(products);

        when(productRepository.findByNameStartingWithIgnoreCaseAndDeletedFalse(eq(prefix), any(Pageable.class)))
                .thenReturn(productPage);

        when(modelMapper.map(product1, ProductDto.class)).thenReturn(createProductDto(product1.getName(), null));
        when(modelMapper.map(product2, ProductDto.class)).thenReturn(createProductDto(product2.getName(), product2.getImage().getImageCode()));

        Page<ProductDto> result = productSearchService.getProductsByPrefix(prefix, pageSize, pageNumber, hideDeleted);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(productRepository, times(1))
                .findByNameStartingWithIgnoreCaseAndDeletedFalse(eq(prefix), any(Pageable.class));

        ProductDto dto1 = result.getContent().get(0);
        ProductDto dto2 = result.getContent().get(1);

        assertNull(dto1.getImageCode());
        assertEquals(124L, dto2.getImageCode());
    }

    @Test
    void whenGetProductsByPrefixWithValidParamsAndHideDeletedFalse_thenReturnAllProducts() {
        String prefix = "validPrefix";
        int pageSize = 1;
        int pageNumber = 1;
        boolean hideDeleted = false;

        Product product1 = createProduct("validPrefixProduct1", false, 123L);
        Product product2 = createProduct("validPrefixProduct2", true, 124L);
        List<Product> products = Arrays.asList(product1, product2);
        Page<Product> productPage = new PageImpl<>(products);

        when(productRepository.findByNameStartingWithIgnoreCase(eq(prefix), any(Pageable.class)))
                .thenReturn(productPage);

        when(modelMapper.map(product1, ProductDto.class)).thenReturn(createProductDto(product1.getName(), product1.getImage().getImageCode()));
        when(modelMapper.map(product2, ProductDto.class)).thenReturn(createProductDto(product2.getName(), product2.getImage().getImageCode()));

        Page<ProductDto> result = productSearchService.getProductsByPrefix(prefix, pageSize, pageNumber, hideDeleted);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(productRepository, times(1))
                .findByNameStartingWithIgnoreCase(eq(prefix), any(Pageable.class));

        result.forEach(dto -> assertNotNull(dto.getImageCode()));
    }

    @Test
    void whenGetProductsByPrefixWithInvalidPrefix_thenThrowInvalidInputException() {
        String invalidPrefix = "123Invalid!";
        int pageSize = 1;
        int pageNumber = 1;
        boolean hideDeleted = true;

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> productSearchService.getProductsByPrefix(invalidPrefix, pageSize, pageNumber, hideDeleted));

        assertEquals("Prefix must be non-empty and contain only letters.", exception.getMessage());
        verify(productRepository, never()).findByNameStartingWithIgnoreCaseAndDeletedFalse(anyString(), any(Pageable.class));
        verify(productRepository, never()).findByNameStartingWithIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    void whenGetProductsByPrefixWithNullPrefix_thenThrowInvalidInputException() {
        int pageSize = 1;
        int pageNumber = 1;
        boolean hideDeleted = true;

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> productSearchService.getProductsByPrefix(null, pageSize, pageNumber, hideDeleted));

        assertEquals("Prefix must be non-empty and contain only letters.", exception.getMessage());
        verify(productRepository, never()).findByNameStartingWithIgnoreCaseAndDeletedFalse(anyString(), any(Pageable.class));
        verify(productRepository, never()).findByNameStartingWithIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    void whenGetProductsByPrefixWithEmptyPrefix_thenThrowInvalidInputException() {
        String emptyPrefix = "";
        int pageSize = 1;
        int pageNumber = 1;
        boolean hideDeleted = true;

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> productSearchService.getProductsByPrefix(emptyPrefix, pageSize, pageNumber, hideDeleted));

        assertEquals("Prefix must be non-empty and contain only letters.", exception.getMessage());
        verify(productRepository, never()).findByNameStartingWithIgnoreCaseAndDeletedFalse(anyString(), any(Pageable.class));
        verify(productRepository, never()).findByNameStartingWithIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    void whenGetProductsByPrefixWithInvalidPageSize_thenThrowInvalidInputException() {
        String prefix = "validPrefix";
        int invalidPageSize = 0;
        int pageNumber = 1;
        boolean hideDeleted = true;

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> productSearchService.getProductsByPrefix(prefix, invalidPageSize, pageNumber, hideDeleted));

        assertEquals("Minimum page size is 1", exception.getMessage());
        verify(productRepository, never()).findByNameStartingWithIgnoreCaseAndDeletedFalse(anyString(), any(Pageable.class));
        verify(productRepository, never()).findByNameStartingWithIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    void whenGetProductsByPrefixWithInvalidPageNumber_thenThrowInvalidInputException() {
        String prefix = "validPrefix";
        int pageSize = 1;
        int invalidPageNumber = 0;
        boolean hideDeleted = true;

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> productSearchService.getProductsByPrefix(prefix, pageSize, invalidPageNumber, hideDeleted));

        assertEquals("Page number must be at least 1", exception.getMessage());
        verify(productRepository, never()).findByNameStartingWithIgnoreCaseAndDeletedFalse(anyString(), any(Pageable.class));
        verify(productRepository, never()).findByNameStartingWithIgnoreCase(anyString(), any(Pageable.class));
    }


    @Test
    void whenGetProductImageByImageCodeWithValidImageCode_thenReturnImageDto() {
        Long imageCode = 123L;
        Image image = createImage(imageCode);
        Product product = createProduct("productName", false, imageCode);

        when(imageRepository.findByImageCodeAndDeletedFalse(imageCode)).thenReturn(Optional.of(image));
        when(productRepository.findByImageCode(imageCode)).thenReturn(Optional.of(product));
        when(modelMapper.map(image, ImageDto.class)).thenReturn(createImageDto(imageCode));

        ImageDto result = productSearchService.getProductImageByImageCode(imageCode);

        assertNotNull(result);
        assertEquals(imageCode, result.getImageCode());
        verify(imageRepository, times(1)).findByImageCodeAndDeletedFalse(imageCode);
        verify(productRepository, times(1)).findByImageCode(imageCode);
        verify(modelMapper, times(1)).map(image, ImageDto.class);
    }

    @Test
    void whenGetProductImageByImageCodeWithInvalidImageCode_thenThrowImageNotFoundException() {
        Long imageCode = 123L;

        when(imageRepository.findByImageCodeAndDeletedFalse(imageCode)).thenReturn(Optional.empty());

        ImageNotFoundException exception = assertThrows(ImageNotFoundException.class,
                () -> productSearchService.getProductImageByImageCode(imageCode));

        assertEquals(String.format("Image with Code %d not found.", imageCode), exception.getMessage());
        verify(imageRepository, times(1)).findByImageCodeAndDeletedFalse(imageCode);
        verify(productRepository, never()).findByImageCode(imageCode);
        verify(modelMapper, never()).map(any(Image.class), any(ImageDto.class));
    }

    @Test
    void whenGetProductImageByImageCodeWithImageCodeNotBelongingToProduct_thenThrowProductNotFoundException() {
        Long imageCode = 123L;
        Image image = createImage(imageCode);

        when(imageRepository.findByImageCodeAndDeletedFalse(imageCode)).thenReturn(Optional.of(image));
        when(productRepository.findByImageCode(imageCode)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productSearchService.getProductImageByImageCode(imageCode));

        assertEquals(String.format("Image with Code %d does not belong to any product.", imageCode), exception.getMessage());
        verify(imageRepository, times(1)).findByImageCodeAndDeletedFalse(imageCode);
        verify(productRepository, times(1)).findByImageCode(imageCode);
        verify(modelMapper, never()).map(any(Image.class), any(ImageDto.class));
    }

    @Test
    void whenGetProductImageByImageCodeWithDeletedProduct_thenThrowProductNotFoundException() {
        Long imageCode = 123L;
        Image image = createImage(imageCode);
        Product product = createProduct("productName", true, imageCode);

        when(imageRepository.findByImageCodeAndDeletedFalse(imageCode)).thenReturn(Optional.of(image));
        when(productRepository.findByImageCode(imageCode)).thenReturn(Optional.of(product));

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productSearchService.getProductImageByImageCode(imageCode));

        assertEquals(String.format("Product with image code %d not found", imageCode), exception.getMessage());
        verify(imageRepository, times(1)).findByImageCodeAndDeletedFalse(imageCode);
        verify(productRepository, times(1)).findByImageCode(imageCode);
        verify(modelMapper, never()).map(any(Image.class), any(ImageDto.class));
    }


    @Test
    void whenGetProductsByCriteriaWithAllCriteria_thenReturnProducts() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setPage(1);
        criteria.setSize(10);
        criteria.setSortBy("price");
        criteria.setSortDir("desc");
        criteria.setMinPrice(BigDecimal.valueOf(10));
        criteria.setMaxPrice(BigDecimal.valueOf(100));
        criteria.setMinStock(5);
        criteria.setMaxStock(50);
        criteria.setDeleted(false);
        criteria.setHasImage(true);
        criteria.setCreationDateStart("2023-01-01T00:00:00");
        criteria.setCreationDateEnd("2023-12-31T23:59:59");
        criteria.setLastUpdateDateStart("2023-01-01T00:00:00");
        criteria.setLastUpdateDateEnd("2023-12-31T23:59:59");

        Product product = new Product();
        Image image = new Image();
        image.setImageCode(123L);
        product.setImage(image);

        Page<Product> productPage = new PageImpl<>(List.of(product));
        ProductDto productDto = new ProductDto();
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);
        when(modelMapper.map(product, ProductDto.class)).thenReturn(productDto);

        Page<ProductDto> result = productSearchService.getProductsByCriteria(criteria);

        assertEquals(1, result.getTotalElements());
        assertEquals(productDto, result.getContent().get(0));
        assertEquals(123L, productDto.getImageCode());

        //---
        ArgumentCaptor<Specification> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(productRepository).findAll(specCaptor.capture(), any(Pageable.class));
        Specification<Product> capturedSpec = specCaptor.getValue();

        assertNotNull(capturedSpec);

        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        Predicate predicate = capturedSpec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(predicate);
        //---
    }

    @Test
    void whenGetProductsByCriteriaWithNoCriteria_thenReturnProducts() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setPage(1);
        criteria.setSize(10);
        criteria.setSortBy("price");
        criteria.setSortDir("asc");

        Product product = new Product();
        Page<Product> productPage = new PageImpl<>(List.of(product));
        ProductDto productDto = new ProductDto();
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);
        when(modelMapper.map(product, ProductDto.class)).thenReturn(productDto);

        Page<ProductDto> result = productSearchService.getProductsByCriteria(criteria);

        assertEquals(1, result.getTotalElements());
        assertEquals(productDto, result.getContent().get(0));

        //--
        ArgumentCaptor<Specification> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(productRepository).findAll(specCaptor.capture(), any(Pageable.class));
        Specification<Product> capturedSpec = specCaptor.getValue();

        assertNotNull(capturedSpec);

        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        Predicate predicate = capturedSpec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(predicate);
        //--
    }

    @Test
    void whenGetProductsByCriteriaWithInvalidDate_thenThrowInvalidInputException() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setPage(1);
        criteria.setSize(10);
        criteria.setSortBy("price");
        criteria.setSortDir("asc");
        criteria.setCreationDateStart("invalid-date-format");

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> productSearchService.getProductsByCriteria(criteria));

        assertEquals("Date must be in the format 'yyyy-MM-ddTHH:mm:ss'", exception.getMessage());
    }



    private Product createProduct(String name, boolean deleted, Long imageCode) {
        Product product = new Product();
        product.setName(name);
        product.setDeleted(deleted);
        if (imageCode != null) {
            Image image = new Image();
            image.setImageCode(imageCode);
            product.setImage(image);
        }
        return product;
    }

    private ProductDto createProductDto(String name, Long imageCode) {
        ProductDto productDto = new ProductDto();
        productDto.setName(name);
        if (imageCode != null) {
            productDto.setImageCode(imageCode);
        }
        return productDto;
    }

    private Image createImage(Long imageCode) {
        Image image = new Image();
        image.setImageCode(imageCode);
        return image;
    }

    private ImageDto createImageDto(Long imageCode) {
        ImageDto imageDto = new ImageDto();
        imageDto.setImageCode(imageCode);
        return imageDto;
    }
}