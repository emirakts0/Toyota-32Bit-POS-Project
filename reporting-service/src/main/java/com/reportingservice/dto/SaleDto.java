package com.reportingservice.dto;

import com.reportingservice.model.DiscountType;
import com.reportingservice.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class SaleDto {

    private Long id;
    private String cashierName;

    private BigDecimal totalPrice;
    private BigDecimal discountedPrice;

    private String campaignName;
    private Long campaignId;
    private DiscountType discountType;
    private double discountValue;

    private BigDecimal amountReceived;
    private BigDecimal change;

    private PaymentMethod paymentMethod;

    private LocalDateTime saleDate;
    private boolean isCancelled;

    private List<SaleItemDto> saleItems = new ArrayList<>();
}
