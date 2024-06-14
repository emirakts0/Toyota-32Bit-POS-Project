package com.reportingservice.service;

import com.reportingservice.dto.ReceiptDto;
import jakarta.transaction.Transactional;

public interface ReceiptTrackingService {

    @Transactional
    String initializeReceiptCache(Long saleId, String eventId);

    @Transactional
    void updateReceiptStatus(String requestId, String status, byte[] receiptData);

    ReceiptDto getReceiptStatus(String requestId);
}
