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
import com.productservice.service.ProductSearchService;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;
    private final ModelMapper modelMapper;


    @Transactional
    @Override
    public ProductDto getProductByBarcode(String barcode) {

        Product product = productRepository.findByBarcodeAndDeletedFalse(barcode)
                .orElseThrow(() -> new ProductNotFoundException(String.format("Product with barcode %s not found", barcode)));
        return modelMapper.map(product, ProductDto.class);
    }


    @Transactional
    @Override
    public Page<ProductDto> getProductsByPrefix(String prefix,
                                                int pageSize,
                                                int pageNumber,
                                                boolean hideDeleted) {

        if (prefix == null || prefix.trim().isEmpty() || !prefix.matches("^[\\p{L}\\s]+$")) {
            throw new InvalidInputException("Prefix must be non-empty and contain only letters.");}
        if (pageSize < 1) {
            throw new InvalidInputException("Minimum page size is 1");}
        if (pageNumber < 1) {
            throw new InvalidInputException("Page number must be at least 1");
        }

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by("name").ascending());

        Page<Product> productPage = hideDeleted
                ? productRepository.findByNameStartingWithIgnoreCaseAndDeletedFalse(prefix, pageable)
                : productRepository.findByNameStartingWithIgnoreCase(prefix, pageable);

        return productPage.map(product -> modelMapper.map(product, ProductDto.class));
    }


    @Transactional
    @Override
    public ImageDto getProductImageByImageCode(Long imageCode){

        Image image = imageRepository.findByImageCodeAndDeletedFalse(imageCode)
                .orElseThrow(() -> new ImageNotFoundException(String.format("Image with Code %d not found.", imageCode)));


        Product product = productRepository.findByImageCode(imageCode)
                .orElseThrow(() -> new ProductNotFoundException(String.format("Image with Code %d does not belong to any product.", imageCode)));
        if (product.isDeleted()){
            throw new ProductNotFoundException(String.format("Product with image code %d not found", imageCode));
        }

        return modelMapper.map(image, ImageDto.class);
    }


    @Override
    public Page<ProductDto> getProductsByCriteria(ProductSearchCriteria criteria) {

        Pageable pageable = PageRequest.of(
                criteria.getPage() - 1,
                criteria.getSize(),
                Sort.by(criteria.getSortDir().equalsIgnoreCase("desc")
                                ? Sort.Direction.DESC : Sort.Direction.ASC,
                        criteria.getSortBy()));

        LocalDateTime creationDateStart = parseDate(criteria.getCreationDateStart());
        LocalDateTime creationDateEnd = parseDate(criteria.getCreationDateEnd());
        LocalDateTime lastUpdateDateStart = parseDate(criteria.getLastUpdateDateStart());
        LocalDateTime lastUpdateDateEnd = parseDate(criteria.getLastUpdateDateEnd());

        Specification<Product> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));}

            if (criteria.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));}

            if (criteria.getMinStock() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("stock"), criteria.getMinStock()));}

            if (criteria.getMaxStock() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("stock"), criteria.getMaxStock()));}

            if (criteria.getDeleted() != null) {
                predicates.add(criteriaBuilder.equal(root.get("deleted"), criteria.getDeleted()));}

            if (criteria.getHasImage() != null) {
                predicates.add(criteriaBuilder.equal(root.get("hasImage"), criteria.getHasImage()));}

            if (creationDateStart != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("creationDate"), creationDateStart));}

            if (creationDateEnd != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("creationDate"), creationDateEnd));}

            if (lastUpdateDateStart != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("lastUpdateDate"), lastUpdateDateStart));}

            if (lastUpdateDateEnd != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("lastUpdateDate"), lastUpdateDateEnd));}

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productsPage = productRepository.findAll(specification, pageable);
        return productsPage.map(product -> modelMapper.map(product, ProductDto.class));
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Date must be in the format 'yyyy-MM-ddTHH:mm:ss'");
        }
    }
}
