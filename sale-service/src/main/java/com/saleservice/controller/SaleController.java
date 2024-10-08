package com.saleservice.controller;

import com.saleservice.dto.CompleteSale;
import com.saleservice.dto.ReceiptMessage;
import com.saleservice.model.PaymentMethod;
import com.saleservice.service.SaleService;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/sale")
public class SaleController {

    private final SaleService saleService;


    @PostMapping("/{bagId}")
    public ResponseEntity<ReceiptMessage> completeSale(@PathVariable Long bagId,
                                                       @RequestBody CompleteSale completeSale,
                                                       @RequestHeader("Name") String cashierName) {
        log.trace("completeSale endpoint called with bagId: {}, amountReceived: {}, paymentMethod: {}, cashierName: {}",
                bagId, completeSale.getAmountReceived(), completeSale.getPaymentMethod(), cashierName);

        ReceiptMessage message = saleService.completeSale(  bagId,
                completeSale.getAmountReceived(),
                completeSale.getPaymentMethod(),
                cashierName);
        return ResponseEntity.ok(message);
    }


    @DeleteMapping("/{saleId}/cancel")
    public ResponseEntity<String> cancelSale(@PathVariable Long saleId) {
        log.trace("cancelSale endpoint called with saleId: {}", saleId);

        saleService.cancelSale(saleId);
        return ResponseEntity.ok().body("Sale successfully cancelled.");
    }
}

