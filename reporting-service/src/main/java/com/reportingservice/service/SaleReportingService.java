package com.reportingservice.service;

import com.reportingservice.dto.SaleDto;
import com.reportingservice.dto.SaleSearchCriteria;
import com.reportingservice.dto.SaleSearchCriteriaWithPagination;
import com.reportingservice.exception.InvalidInputException;
import com.reportingservice.exception.SaleNotFoundException;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for sales reporting.
 * Provides methods for generating receipts, retrieving sales, and searching sales by criteria.
 * @author Emir Aktaş
 */
public interface SaleReportingService {

    /**
     * Generates a receipt for the sale identified by the given ID.
     *
     * @param id the ID of the sale
     * @return the request ID for tracking the receipt generation
     * @throws SaleNotFoundException if the sale with the given ID is not found
     */
    String generateReceiptById(Long id);


    /**
     * Retrieves the sale identified by the given ID.
     *
     * @param saleId the ID of the sale
     * @return the sale details as a SaleDto
     * @throws SaleNotFoundException if the sale with the given ID is not found
     */
    SaleDto getSaleById(Long saleId);


    /**
     * Retrieves a paginated list of sales matching the given search criteria.
     *
     * @param criteria the search criteria with pagination details
     * @return a paginated list of sales matching the criteria
     * @throws InvalidInputException if the date format in the criteria is invalid
     */
    Page<SaleDto> getSalesByCriteriaWithPagination(SaleSearchCriteriaWithPagination criteria);


    /**
     * Retrieves a list of sales matching the given search criteria.
     *
     * @param criteria the search criteria
     * @return a list of sales matching the criteria
     * @throws InvalidInputException if the date format in the criteria is invalid
     */
    List<SaleDto> getSalesByCriteria(SaleSearchCriteria criteria);
}
