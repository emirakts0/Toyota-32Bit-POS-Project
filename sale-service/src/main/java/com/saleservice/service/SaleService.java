package com.saleservice.service;

import com.saleservice.dto.ReceiptMessage;
import com.saleservice.exception.*;
import com.saleservice.model.PaymentMethod;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;

/**
 * Service interface for managing sales.
 * Provides methods for completing and canceling sales transactions.
 * @author Emir Aktaş
 */
public interface SaleService {

    /**
     * Completes a sale based on the given bag ID, amount received, payment method, and cashier name.
     *
     * @param bagId the ID of the bag
     * @param amountReceived the amount of money received for the sale
     * @param paymentMethod the method of payment used for the sale
     * @param cashierName the name of the cashier processing the sale
     * @return the receipt message containing details of the completed sale
     * @throws BagNotFoundException if the bag with the given ID is not found
     * @throws BagIsEmptyException if the bag is empty
     * @throws InvalidInputException if the received amount is less than the total sale price
     */
    @Transactional
    ReceiptMessage completeSale(Long bagId,
                                BigDecimal amountReceived,
                                PaymentMethod paymentMethod,
                                String cashierName);


    /**
     * Cancels a sale based on the given sale ID.
     *
     * @param saleId the ID of the sale to be canceled
     * @throws SaleNotFoundException if the sale with the given ID is not found
     * @throws SaleAlreadyCancelledException if the sale is already canceled
     */
    @Transactional
    void cancelSale(Long saleId);
}
