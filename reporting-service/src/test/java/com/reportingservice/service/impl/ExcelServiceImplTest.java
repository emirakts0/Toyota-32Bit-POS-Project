package com.reportingservice.service.impl;

import com.reportingservice.dto.ExcelReportMessage;
import com.reportingservice.dto.SaleDto;
import com.reportingservice.dto.SaleSearchCriteria;
import com.reportingservice.service.SaleReportingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExcelServiceImplTest {

    @InjectMocks
    private ExcelServiceImpl excelService;

    @Mock
    private SaleReportingService saleReportingService;

    @Mock
    private AmqpTemplate rabbitTemplate;

    @Value("${excel.rabbitmq.queue}")
    private String queueName = "test-queue";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(excelService, "queueName", queueName);
    }

    @Test
    void whenGenerateSalesExcelReportWithValidCriteria_thenExcelReportGeneratedSuccessfully() {
        SaleSearchCriteria criteria = new SaleSearchCriteria();
        List<SaleDto> salesList = new ArrayList<>();

        when(saleReportingService.getSalesByCriteria(any(SaleSearchCriteria.class))).thenReturn(salesList);
        ByteArrayInputStream result = excelService.generateSalesExcelReport(criteria);

        assertNotNull(result, "Result should not be null");
        verify(saleReportingService).getSalesByCriteria(criteria);
    }

    @Test
    void whenEnqueueExcelReportRequestWithValidCriteriaAndEmail_thenEnqueuedExcelReportSuccessfully() {
        SaleSearchCriteria criteria = new SaleSearchCriteria();
        String email = "test@example.com";

        excelService.enqueueExcelReportRequest(criteria, email);

        ArgumentCaptor<ExcelReportMessage> captor = ArgumentCaptor.forClass(ExcelReportMessage.class);
        verify(rabbitTemplate, times(1)).convertAndSend(eq(queueName), captor.capture());

        ExcelReportMessage capturedMessage = captor.getValue();
        assertNotNull(capturedMessage);
        assertNotNull(capturedMessage.getCriteria());
        assertNotNull(capturedMessage.getMail());
    }
}