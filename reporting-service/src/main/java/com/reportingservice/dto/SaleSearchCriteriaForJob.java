package com.reportingservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

@Data
public class SaleSearchCriteriaForJob implements Serializable {

    @DecimalMin(value = "0.0", inclusive = false, message = "The lowest total price can be 0.")
    private Integer minTotalPrice;

    private Integer maxTotalPrice;
    private Boolean hasCampaign;
    private Integer campaignId;
    private String paymentMethod;
    private Boolean includeCanceled;

    @Min(value = 1, message = "Lookback period must be at least 1 day")
    private int lookbackPeriodInDays;
}
