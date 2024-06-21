package com.reportingservice.service.impl;

import com.reportingservice.dto.ReceiptDto;
import com.reportingservice.model.Receipt;
import com.reportingservice.repository.ReceiptRepository;
import com.reportingservice.service.ReceiptTrackingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptTrackingServiceImpl implements ReceiptTrackingService {

    private final ReceiptRepository receiptRepository;
    private final ModelMapper modelMapper;
    private static final long EXPIRATION_TIME = 300L;  // in seconds


    @Override
    @Transactional
    public String initializeReceiptCache(Long saleId, String eventId) {
        log.trace("initializeReceiptCache method begins. SaleId: {}, EventId: {}", saleId, eventId);

        String requestId = UUID.randomUUID().toString();

        if (eventId != null) {
            requestId = eventId;
        }

        Receipt receipt = new Receipt();
        receipt.setId(requestId);
        receipt.setStatus("PENDING");
        receipt.setSaleId(saleId);
        receipt.setExpiration(EXPIRATION_TIME);

        receiptRepository.save(receipt);
        log.info("initializeReceiptCache: Receipt initialized with requestId: {} for SaleId: {}", requestId, saleId);

        log.trace("initializeReceiptCache method ends. RequestId: {}", requestId);
        return requestId;
    }


    @Override
    @Transactional
    public void updateReceiptStatus(String requestId, String status, byte[] receiptData) {
        log.trace("updateReceiptStatus method begins. RequestId: {}, Status: {}", requestId, status);

        Receipt receipt = receiptRepository.findById(requestId)
                .orElse(new Receipt());

        receipt.setId(requestId);
        receipt.setStatus(status);
        receipt.setReceiptData(receiptData);
        receipt.setExpiration(EXPIRATION_TIME);

        receiptRepository.save(receipt);
        log.info("updateReceiptStatus: Receipt updated with requestId: {}, Status: {}", requestId, status);

        log.trace("updateReceiptStatus method ends. RequestId: {}", requestId);
    }


    @Override
    public ReceiptDto getReceiptStatus(String requestId) {
        log.trace("getReceiptStatus method begins. RequestId: {}", requestId);

        Receipt receipt = receiptRepository.findById(requestId)
                .orElse(null);
        if (receipt == null) {
            log.warn("getReceiptStatus: Receipt not found for requestId: {}", requestId);
            return null;
        }
        ReceiptDto receiptDto = modelMapper.map(receipt, ReceiptDto.class);
        log.info("getReceiptStatus: Receipt found for requestId: {}", requestId);

        log.trace("getReceiptStatus method ends. RequestId: {}", requestId);
        return receiptDto;
    }
}
