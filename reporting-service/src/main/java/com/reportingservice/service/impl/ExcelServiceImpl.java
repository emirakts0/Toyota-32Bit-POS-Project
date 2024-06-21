package com.reportingservice.service.impl;

import com.reportingservice.dto.ExcelReportMessage;
import com.reportingservice.dto.SaleDto;
import com.reportingservice.dto.SaleSearchCriteria;
import com.reportingservice.service.ExcelService;
import com.reportingservice.service.SaleReportingService;
import com.reportingservice.utility.ExcelReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExcelServiceImpl implements ExcelService {

    private final SaleReportingService saleReportingService;
    private final AmqpTemplate rabbitTemplate;

    @Value("${excel.rabbitmq.queue}")
    private String queueName;


    @Override
    public ByteArrayInputStream generateSalesExcelReport(SaleSearchCriteria criteria) {
        log.trace("generateSalesExcelReport method begins. Criteria: {}", criteria);

        List<SaleDto> salesList = saleReportingService.getSalesByCriteria(criteria);
        ByteArrayInputStream byteArrayInputStream = ExcelReportGenerator.generateSalesExcelFile(salesList);

        log.trace("generateSalesExcelReport method ends. Criteria: {}", criteria);
        return byteArrayInputStream;
    }


    @Override
    public void enqueueExcelReportRequest(SaleSearchCriteria criteria, String email) {
        log.trace("requestExcelReport method begins. Criteria: {}, Email: {}", criteria, email);

        ExcelReportMessage message = new ExcelReportMessage(email, criteria, 0);

        rabbitTemplate.convertAndSend(queueName, message);
        log.info("requestExcelReport: Excel report request sent to queue with Criteria: {}", criteria);

        log.trace("requestExcelReport method ends. Criteria: {}, Email: {}", criteria, email);
    }
}