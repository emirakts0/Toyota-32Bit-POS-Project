package com.reportingservice.service;

import com.reportingservice.dto.SaleSearchCriteria;

import java.io.ByteArrayInputStream;

public interface ExcelService {

    ByteArrayInputStream generateSalesExcelReport(SaleSearchCriteria criteria);

    void enqueueExcelReportRequest(SaleSearchCriteria criteria, String email);

}
