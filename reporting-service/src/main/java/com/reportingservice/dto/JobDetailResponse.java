package com.reportingservice.dto;

import lombok.Data;

@Data
public class JobDetailResponse {
    private String jobName;
    private String email;
    private String nextFireTime;
    private SaleSearchCriteriaForJob criteria;
}
