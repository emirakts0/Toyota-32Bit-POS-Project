package com.reportingservice.service;

import com.reportingservice.dto.ReceiptDto;
import jakarta.transaction.Transactional;

/**
 * Service interface for tracking receipt generation status.
 * Provides methods for initializing receipt cache, updating receipt status, and retrieving receipt status.
 * @author Emir Aktaş
 */
public interface ReceiptTrackingService {

    /**
     * Initializes the receipt cache for a given sale ID and event ID.
     *
     * @param saleId the ID of the sale
     * @param eventId the ID of the event
     * @return the generated request ID for the receipt
     */
    @Transactional
    String initializeReceiptCache(Long saleId, String eventId);


    /**
     * Updates the status of the receipt identified by the given request ID.
     *
     * @param requestId the ID of the receipt request
     * @param status the status of the receipt (e.g., "PENDING", "COMPLETED", "FAILED")
     * @param receiptData the byte array containing the receipt data (if any)
     */
    @Transactional
    void updateReceiptStatus(String requestId, String status, byte[] receiptData);


    /**
     * Retrieves the status of the receipt identified by the given request ID.
     *
     * @param requestId the ID of the receipt request
     * @return the receipt status as a ReceiptDto
     */
    ReceiptDto getReceiptStatus(String requestId);
}
