package com.reportingservice.service;

import com.reportingservice.dto.SaleDto;
import com.reportingservice.dto.SaleSearchCriteria;
import org.springframework.data.domain.Page;

public interface SaleReportingService {

    String generateReceiptById(Long id);

    SaleDto getSaleById(Long saleId);

    Page<SaleDto> getSalesByCriteria(SaleSearchCriteria criteria);
}
