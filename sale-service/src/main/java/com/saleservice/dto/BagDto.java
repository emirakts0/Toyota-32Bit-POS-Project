package com.saleservice.dto;

import com.saleservice.model.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BagDto {

    private Long id;
    private BigDecimal totalPrice;

    private Long campaignId;
    private String campaignName;
    private DiscountType discountType;
    private double discountValue;
    private BigDecimal discountedPrice;

    private List<BagItemDto> items;
}
