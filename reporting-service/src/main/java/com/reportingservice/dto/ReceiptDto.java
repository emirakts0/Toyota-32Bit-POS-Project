package com.reportingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReceiptDto {
    private String id;
    private String status;
    private Long saleId;
    private byte[] receiptData;
}
