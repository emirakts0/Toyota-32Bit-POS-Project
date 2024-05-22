package com.productservice.dto;


import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSearchCriteria {
    @Min(value = 1, message = "Page index must be at least 1")
    private Integer page = 1;

    @Min(value = 1, message = "Page size must be at least 1")
    private Integer size = 10;

    @Pattern(regexp = "name|price|stock|creationDate|lastUpdateDate", message = "Invalid sort by value. Allowed values are 'name', 'price', 'stock', 'creationDate', 'lastUpdateDate'.")
    private String sortBy = "name";

    @Pattern(regexp = "asc|desc", message = "Invalid sort direction. Valid values are 'asc' or 'desc'.")
    private String sortDir = "asc";

    @DecimalMin(value = "0.0", inclusive = false, message = "Min price must be greater than zero")
    private BigDecimal minPrice;

    @DecimalMax(value = "1000000.0", message = "Max price must be less than 1,000,000")
    private BigDecimal maxPrice;

    @Min(value = 0, message = "Min stock must be at least 0")
    private Integer minStock;

    @Max(value = 1000000, message = "Max stock must be less than or equal to 1,000,000")
    private Integer maxStock;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$", message = "Date must be in the format 'yyyy-MM-ddTHH:mm:ss'")
    private String creationDateStart;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$", message = "Date must be in the format 'yyyy-MM-ddTHH:mm:ss'")
    private String creationDateEnd;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$", message = "Date must be in the format 'yyyy-MM-ddTHH:mm:ss'")
    private String lastUpdateDateStart;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$", message = "Date must be in the format 'yyyy-MM-ddTHH:mm:ss'")
    private String lastUpdateDateEnd;

    private Boolean deleted;
    private Boolean hasImage;
}