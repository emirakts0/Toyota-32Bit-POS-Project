package com.saleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BagItemDto {
    private String barcode;
    private int quantity;
    private BigDecimal price;
    private String name;
}
