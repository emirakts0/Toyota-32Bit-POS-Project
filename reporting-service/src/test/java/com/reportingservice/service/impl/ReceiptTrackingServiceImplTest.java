package com.reportingservice.service.impl;

import com.reportingservice.dto.ReceiptDto;
import com.reportingservice.model.Receipt;
import com.reportingservice.repository.ReceiptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReceiptTrackingServiceImplTest {

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ReceiptTrackingServiceImpl receiptTrackingService;

    private static final long EXPIRATION_TIME = 300L;


    @Test
    void whenInitializeReceiptCacheWithNullEventId_thenNewRequestIdGenerated() {
        Long saleId = 1L;

        String requestId = receiptTrackingService.initializeReceiptCache(saleId, null);

        assertNotNull(requestId);

        verify(receiptRepository, times(1)).save(any(Receipt.class));
    }

    @Test
    void whenInitializeReceiptCacheWithNonNullEventId_thenExistingEventIdUsed() {
        Long saleId = 1L;
        String eventId = UUID.randomUUID().toString();

        String requestId = receiptTrackingService.initializeReceiptCache(saleId, eventId);

        assertNotNull(requestId);
        assertEquals(eventId, requestId);

        verify(receiptRepository, times(1)).save(any(Receipt.class));
    }


    @Test
    void whenUpdateReceiptStatusWithExistingReceipt_thenReceiptUpdatedSuccessfully() {
        String requestId = "existingRequestId";
        String status = "COMPLETED";
        byte[] receiptData = new byte[]{1, 2, 3};

        Receipt existingReceipt = new Receipt();
        existingReceipt.setId(requestId);
        existingReceipt.setStatus("PENDING");

        when(receiptRepository.findById(requestId)).thenReturn(Optional.of(existingReceipt));

        receiptTrackingService.updateReceiptStatus(requestId, status, receiptData);

        verify(receiptRepository, times(1)).findById(requestId);
    }

    @Test
    void whenUpdateReceiptStatusWithNonExistingReceipt_thenNewReceiptCreatedAndUpdatedSuccessfully() {
        String requestId = "newRequestId";
        String status = "COMPLETED";
        byte[] receiptData = new byte[]{1, 2, 3};

        when(receiptRepository.findById(requestId)).thenReturn(Optional.empty());

        receiptTrackingService.updateReceiptStatus(requestId, status, receiptData);

        verify(receiptRepository, times(1)).findById(requestId);
    }


    @Test
    void whenGetReceiptStatusWithExistingReceipt_thenReturnReceiptDto() {
        String requestId = "existingRequestId";
        Receipt receipt = new Receipt();
        receipt.setId(requestId);
        receipt.setStatus("COMPLETED");
        receipt.setReceiptData(new byte[]{1, 2, 3});

        ReceiptDto receiptDto = new ReceiptDto();
        receiptDto.setId(requestId);
        receiptDto.setStatus("COMPLETED");

        when(receiptRepository.findById(requestId)).thenReturn(Optional.of(receipt));
        when(modelMapper.map(receipt, ReceiptDto.class)).thenReturn(receiptDto);

        ReceiptDto result = receiptTrackingService.getReceiptStatus(requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals("COMPLETED", result.getStatus());

        verify(receiptRepository, times(1)).findById(requestId);
        verify(modelMapper, times(1)).map(receipt, ReceiptDto.class);
    }

    @Test
    void whenGetReceiptStatusWithNonExistingReceipt_thenReturnNull() {
        String requestId = "nonExistingRequestId";

        when(receiptRepository.findById(requestId)).thenReturn(Optional.empty());

        ReceiptDto result = receiptTrackingService.getReceiptStatus(requestId);

        assertNull(result);

        verify(receiptRepository, times(1)).findById(requestId);
        verify(modelMapper, never()).map(any(Receipt.class), any(ReceiptDto.class));
    }
}