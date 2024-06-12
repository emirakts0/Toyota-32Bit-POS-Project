package com.saleservice.service;

import com.saleservice.dto.ReceiptMessage;
import com.saleservice.model.PaymentMethod;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;


public interface SaleService {

    @Transactional
    ReceiptMessage completeSale(Long bagId,
                                BigDecimal amountReceived,
                                PaymentMethod paymentMethod,
                                String cashierName);

    @Transactional
    void cancelSale(Long saleId);
}
