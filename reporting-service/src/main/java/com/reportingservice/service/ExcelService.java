package com.reportingservice.service;

import com.reportingservice.dto.SaleSearchCriteria;
import java.io.ByteArrayInputStream;

/**
 * Service interface for generating and handling Excel reports.
 * Provides methods for generating sales reports in Excel format and enqueuing report requests.
 * @author Emir Aktaş
 */
public interface ExcelService {

    /**
     * Generates an Excel report for sales based on the given search criteria.
     *
     * @param criteria the criteria for searching sales
     * @return a ByteArrayInputStream containing the generated Excel report
     */
    ByteArrayInputStream generateSalesExcelReport(SaleSearchCriteria criteria);


    /**
     * Enqueues a request to generate an Excel report for sales based on the given search criteria.
     * The report will be sent to the specified email address.
     *
     * @param criteria the criteria for searching sales
     * @param email the email address to send the report to
     */
    void enqueueExcelReportRequest(SaleSearchCriteria criteria, String email);
}
