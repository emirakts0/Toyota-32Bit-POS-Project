package com.saleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StockUpdateMessage {

    private String barcode;
    private int stock;
    private int retryCount;
}
