package com.reportingservice.controller;

import com.reportingservice.dto.SaleDto;
import com.reportingservice.dto.SaleSearchCriteria;
import com.reportingservice.dto.SaleSearchCriteriaWithPagination;
import com.reportingservice.service.ExcelService;
import com.reportingservice.service.SaleReportingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("report")
public class ReportingController {

    private final SaleReportingService saleReportingService;
    private final ExcelService excelService;


    @PostMapping("/generate-receipt/{saleId}")
    public ResponseEntity<String> generateReceipt(@PathVariable Long saleId) {
        log.trace("generateReceipt endpoint called for saleId: {}", saleId);

        String requestId = saleReportingService.generateReceiptById(saleId);
        return ResponseEntity.ok("Tracking request id: " + requestId);
    }


    @GetMapping("/{saleId}")
    public ResponseEntity<SaleDto> getSaleById(@PathVariable Long saleId) {
        log.trace("getSaleById endpoint called for saleId: {}", saleId);

        SaleDto saleDto = saleReportingService.getSaleById(saleId);
        return ResponseEntity.ok().body(saleDto);
    }


    @GetMapping
    public ResponseEntity<Page<SaleDto>> getSalesByCriteriaWithPagination(
            @RequestBody @Valid SaleSearchCriteriaWithPagination criteria) {
        log.trace("getSalesByCriteria endpoint called with criteria: {}", criteria);

        Page<SaleDto> salePage = saleReportingService.getSalesByCriteriaWithPagination(criteria);
        return ResponseEntity.ok().body(salePage);
    }


    @GetMapping("/excel")
    public ResponseEntity<String> getExcelReport(@RequestBody @Valid SaleSearchCriteria criteria,
                                                 @RequestParam @Email @NotBlank String email) {
        log.trace("getExcelReport endpoint called for excel");

        excelService.enqueueExcelReportRequest(criteria, email);
        return ResponseEntity.ok("Excel report request received. The report will be sent to: " + email);
    }
}
