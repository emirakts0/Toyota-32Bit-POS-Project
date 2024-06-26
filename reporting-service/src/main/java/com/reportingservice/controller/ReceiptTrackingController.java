package com.reportingservice.controller;

import com.reportingservice.dto.ReceiptDto;
import com.reportingservice.service.ReceiptTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("report/status")
public class ReceiptTrackingController {

    private final ReceiptTrackingService receiptTrackingService;


    @GetMapping("/receipt/{requestId}")
    public ResponseEntity<?> getReceiptStatus(@PathVariable String requestId) {
        log.trace("getReceiptStatus endpoint called for requestId: {}", requestId);

        ReceiptDto receiptDto = receiptTrackingService.getReceiptStatus(requestId);

        if (receiptDto == null) {
            return ResponseEntity.ok().body("Receipt with ID " + requestId + " not found.");}
        if (receiptDto.getReceiptData() == null) {
            return ResponseEntity.ok(receiptDto.getStatus() + " " + requestId );}

        String filename = "receipt_" + receiptDto.getSaleId() + ".pdf";

        log.info("Returning PDF receipt for requestId: {}", requestId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(receiptDto.getReceiptData());
    }
}
