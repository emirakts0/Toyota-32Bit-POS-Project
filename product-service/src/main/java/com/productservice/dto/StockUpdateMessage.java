package com.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class StockUpdateMessage {

    private String barcode;
    private int stock;
    private int retryCount;
}