package com.reportingservice.service;

import com.reportingservice.dto.SaleDto;

public interface PdfGenerationService {
    byte[] generateReceiptPDF(SaleDto saleDto);
}
