package com.reportingservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reportingservice.dto.SaleSearchCriteria;
import com.reportingservice.dto.SaleSearchCriteriaForJob;
import com.reportingservice.service.ExcelService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Scheduled job for generating Excel reports based on sale search criteria.
 * This job is executed by Quartz scheduler.
 * @author Emir Aktaş
 */
@Slf4j
@Component
@NoArgsConstructor
public class ScheduledJob implements Job {

    @Autowired
    private ExcelService excelService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ModelMapper modelMapper;


    /**
     * Executes the job to generate an Excel report.
     * This method is called by the Quartz scheduler.
     *
     * @param context the job execution context
     * @throws JobExecutionException if there is an error during job execution
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.trace("execute method begins.");

        try {
            String criteriaJson = context.getMergedJobDataMap().getString("criteria");
            SaleSearchCriteriaForJob criteriaForJob = objectMapper.readValue(criteriaJson, SaleSearchCriteriaForJob.class);
            SaleSearchCriteria criteria = convertToSaleSearchCriteria(criteriaForJob);

            String email = context.getMergedJobDataMap().getString("email");

            excelService.enqueueExcelReportRequest(criteria, email);
            log.info("execute(job): Excel report request enqueued with Criteria: {}", criteria);
        } catch (Exception e) {
            log.error("execute(job): Error executing job", e);
            throw new JobExecutionException(e);
        }

        log.trace("execute method ends");
    }


    private SaleSearchCriteria convertToSaleSearchCriteria(SaleSearchCriteriaForJob criteriaForJob) {
        log.trace("convertToSaleSearchCriteria method begins. CriteriaForJob: {}", criteriaForJob);

        SaleSearchCriteria criteria = modelMapper.map(criteriaForJob, SaleSearchCriteria.class);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(criteriaForJob.getLookbackPeriodInDays());

        criteria.setSaleDateStart(startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        criteria.setSaleDateEnd(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        log.trace("convertToSaleSearchCriteria method ends. Criteria: {}", criteria);
        return criteria;
    }
}
