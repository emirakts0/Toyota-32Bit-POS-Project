package com.reportingservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleRequest {

    @Valid
    @NotNull(message = "Criteria must not be null")
    private SaleSearchCriteriaForJob criteria;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email must not be blank")
    private String email;

    @NotBlank(message = "Job name must not be blank")
    private String jobName;

    @Min(value = 1, message = "Days interval must be at least 1")
    private int daysInterval;
}
