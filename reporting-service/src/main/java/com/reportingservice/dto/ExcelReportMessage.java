package com.reportingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExcelReportMessage {
    String mail;
    SaleSearchCriteria criteria;

    int retryCount = 0;
}
