package com.saleservice.dto;

import com.saleservice.model.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class CampaignResponseDto {

    private Long id;

    private String name;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private DiscountType discountType;

    private double discountValue;

    private boolean deleted;
}
