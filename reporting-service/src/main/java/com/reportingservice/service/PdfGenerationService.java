package com.reportingservice.service;

import com.reportingservice.dto.SaleDto;

/**
 * Service interface for generating PDF receipts.
 * Provides a method for generating a receipt PDF based on sale details.
 * @author Emir Aktaş
 */
public interface PdfGenerationService {

    /**
     * Generates a PDF receipt for the given sale details.
     *
     * @param saleDto the sale details
     * @return a byte array containing the generated PDF receipt
     */
    byte[] generateReceiptPDF(SaleDto saleDto);
}
