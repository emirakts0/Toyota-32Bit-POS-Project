package com.saleservice.service;


public interface SaleService {

    @Transactional
    ReceiptMessage completeSale(Long bagId,
                                BigDecimal amountReceived,
                                PaymentMethod paymentMethod,
                                String cashierName);

    @Transactional
    void cancelSale(Long saleId);
}
