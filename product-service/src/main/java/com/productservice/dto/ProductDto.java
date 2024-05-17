package com.productservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDto {

    private String name;

    private String barcode;

    private BigDecimal price;
    private Integer stock;

    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;

    private Long imageCode;

    private boolean hasImage;
    private boolean deleted;
}
