package com.reportingservice.dto;

import com.reportingservice.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SaleSearchCriteria {
    @DecimalMin(value = "0.0", inclusive = false, message = "The lowest total price can be 0.")
    private BigDecimal minTotalPrice;

    private BigDecimal maxTotalPrice;

    private Boolean hasCampaign;
    private Long campaignId;
    private PaymentMethod paymentMethod;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$", message = "Date must be in the format 'yyyy-MM-ddTHH:mm:ss'")
    private String saleDateStart;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$", message = "Date must be in the format 'yyyy-MM-ddTHH:mm:ss'")
    private String saleDateEnd;

    private Boolean includeCanceled;
}
