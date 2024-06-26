package com.saleservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BagItem {
    private String barcode;
    private int quantity;
    private BigDecimal price;
    private String name;
}
