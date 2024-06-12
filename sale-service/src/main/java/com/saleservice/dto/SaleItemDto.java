package com.saleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SaleItemDto {

    private String barcode;
    private String name;
    private int quantity;
    private BigDecimal salePrice;
}
