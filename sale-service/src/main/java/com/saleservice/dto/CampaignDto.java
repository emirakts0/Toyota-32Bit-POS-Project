package com.saleservice.dto;

import com.saleservice.model.DiscountType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class CampaignDto {

    @NotBlank(message = "Campaign name cannot be empty or null.")
    @Size(min = 1, max = 150, message = "Name length must be between 1 and 150 characters.")
    private String name;

    private String startDate;

    @NotBlank(message = "endDate cannot be empty or null.")
    private String endDate;

    @NotNull(message = "discountType cannot be empty or null.")
    private DiscountType discountType;

    @Min(value = 0, message = "Discount value must be greater than or equal to zero.")
    private double discountValue;
}
