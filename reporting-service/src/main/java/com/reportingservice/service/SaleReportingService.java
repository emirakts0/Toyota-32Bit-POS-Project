package com.reportingservice.service;

import com.reportingservice.dto.SaleDto;
import com.reportingservice.dto.SaleSearchCriteria;
import com.reportingservice.dto.SaleSearchCriteriaWithPagination;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SaleReportingService {

    String generateReceiptById(Long id);

    SaleDto getSaleById(Long saleId);

    Page<SaleDto> getSalesByCriteriaWithPagination(SaleSearchCriteriaWithPagination criteria);

    List<SaleDto> getSalesByCriteria(SaleSearchCriteria criteria);
}
