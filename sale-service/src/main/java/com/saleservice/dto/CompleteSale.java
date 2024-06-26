package com.saleservice.dto;

import com.saleservice.model.PaymentMethod;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompleteSale {
    @PositiveOrZero(message = "Amount received must be zero or positive.")
    BigDecimal amountReceived;
    PaymentMethod paymentMethod;
}
