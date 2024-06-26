package com.reportingservice.service.impl;

import com.reportingservice.dto.SaleDto;
import com.reportingservice.dto.SaleItemDto;
import com.reportingservice.utility.MarketInfo;
import com.reportingservice.utility.PdfReceiptGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PdfGenerationServiceImplTest {

    @InjectMocks
    private PdfGenerationServiceImpl pdfGenerationService;


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pdfGenerationService, "marketName", "Test Market");
        ReflectionTestUtils.setField(pdfGenerationService, "marketAddress", "123 Test St");
        ReflectionTestUtils.setField(pdfGenerationService, "marketPhone", "123-456-7890");
        ReflectionTestUtils.setField(pdfGenerationService, "marketCity", "Test City");
    }

    @Test
    void whenGenerateReceiptWithValidSaleDto_thenReceiptGeneratedSuccessfully() {
        SaleDto saleDto = new SaleDto();
        saleDto.setId(1L);
        saleDto.setTotalPrice(BigDecimal.valueOf(100.0));
        SaleItemDto saleItemDto = new SaleItemDto();
        saleItemDto.setBarcode("123456789");
        saleItemDto.setName("Test Product");
        saleItemDto.setQuantity(2);
        saleItemDto.setSalePrice(BigDecimal.valueOf(50.0));
        saleDto.setSaleItems(Collections.singletonList(saleItemDto));

        byte[] expectedPdf = new byte[]{1, 2, 3};

        try (MockedStatic<PdfReceiptGenerator> mockedStatic = mockStatic(PdfReceiptGenerator.class)) {

            mockedStatic.when(() -> PdfReceiptGenerator.createReceipt(any(SaleDto.class), any(MarketInfo.class))).thenReturn(expectedPdf);

            byte[] actualPdf = pdfGenerationService.generateReceiptPDF(saleDto);

            assertNotNull(actualPdf);
            assertArrayEquals(expectedPdf, actualPdf);
            mockedStatic.verify(() -> PdfReceiptGenerator.createReceipt(any(SaleDto.class), any(MarketInfo.class)), times(1));
        }
    }
}