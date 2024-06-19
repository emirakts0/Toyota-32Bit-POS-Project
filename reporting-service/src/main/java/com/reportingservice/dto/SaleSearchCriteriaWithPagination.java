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
public class SaleSearchCriteriaWithPagination {
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

    @Pattern(regexp = "saleDate|totalPrice|discountedPrice", message = "Invalid sort by value. Allowed values are 'saleDate', 'totalPrice', 'discountedPrice'.")
    private String sortBy = "saleDate";

    @Pattern(regexp = "asc|desc", message = "Invalid sort direction. Valid values are 'asc' or 'desc'.")
    private String sortDir = "desc";

    @Min(value = 1, message = "Page index must be at least 1")
    private int page = 1;

    @Min(value = 1, message = "Page size must be at least 1")
    private int size = 10;

    private Boolean includeCanceled;
}
