package com.reportingservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reportingservice.config.ScheduledJob;
import com.reportingservice.dto.JobDetailResponse;
import com.reportingservice.dto.SaleSearchCriteriaForJob;
import com.reportingservice.dto.ScheduleRequest;
import com.reportingservice.exception.*;
import com.reportingservice.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerServiceImpl implements SchedulerService {

    private final SchedulerFactoryBean schedulerFactoryBean;
    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;


    @Override
    public void scheduleJob(ScheduleRequest request) {
        log.trace("scheduleJob method begins. Request: {}", request);

        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            JobKey jobKey = new JobKey(request.getJobName());
            if (scheduler.checkExists(jobKey)) {
                log.warn("scheduleJob: Job with name {} already exists", request.getJobName());
                throw new JobAlreadyExistsException("Job with name " + request.getJobName() + " already exists.");
            }

            String criteriaJson = objectMapper.writeValueAsString(request.getCriteria());

            JobDetail jobDetail = JobBuilder.newJob(ScheduledJob.class)
                    .withIdentity(request.getJobName())
                    .usingJobData("email", request.getEmail())
                    .usingJobData("criteria", criteriaJson)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(request.getJobName() + "Trigger")
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMinutes(request.getDaysInterval())  //TODO
                            //.withIntervalInHours(24 * request.getDaysInterval())
                            .repeatForever())
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("scheduleJob: Scheduled job with name: {}", request.getJobName());
        } catch (SchedulerException e) {
            log.warn("scheduleJob: Error scheduling job: {}", e.getMessage(), e);
            throw new JobSchedulingException("Error scheduling job: " + e.getMessage());
        } catch (JsonProcessingException e) {
            log.warn("scheduleJob: Json processing error", e);
            throw new JsonException("Json file could not be processed");
        }

        log.trace("scheduleJob method ends. Request: {}", request);
    }


    @Override
    public void cancelJob(String jobName) {
        log.trace("cancelJob method begins. JobName: {}", jobName);

        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            JobKey jobKey = new JobKey(jobName);
            if (!scheduler.checkExists(jobKey)) {
                log.warn("cancelJob: Job with name {} does not exists", jobName);
                throw new JobCancellationException("Job with name " + jobName + " does not exists.");
            }

            scheduler.deleteJob(new JobKey(jobName));
            log.info("cancelJob: Cancelled job with name: {}", jobName);

        } catch (SchedulerException e) {
            log.warn("cancelJob: Error cancelling job: {}", e.getMessage(), e);
            throw new JobCancellationException("Error cancelling job: " + e.getMessage());
        }

        log.trace("cancelJob method ends. JobName: {}", jobName);
    }


    @Override
    public List<JobDetailResponse> listAllJobs() {
        log.trace("listAllJobs method begins");

        try {

            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            List<JobDetailResponse> jobDetails = scheduler.getJobKeys(GroupMatcher.anyGroup())
                    .stream()
                    .map(jobKey -> {
                        try {
                            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);

                            String nextFireTime = triggers.stream()
                                    .map(Trigger::getNextFireTime)
                                    .filter(Objects::nonNull)
                                    .map(Object::toString)
                                    .findFirst()
                                    .orElse("No next fire time");

                            JobDataMap dataMap = jobDetail.getJobDataMap();
                            String email = dataMap.getString("email");
                            String criteriaJson = dataMap.getString("criteria");

                            SaleSearchCriteriaForJob criteria = objectMapper.readValue(criteriaJson, SaleSearchCriteriaForJob.class);

                            JobDetailResponse response = new JobDetailResponse();
                            response.setJobName(jobKey.getName());
                            response.setEmail(email);
                            response.setCriteria(criteria);
                            response.setNextFireTime(nextFireTime);

                            return response;
                        } catch (SchedulerException | JsonProcessingException e) {
                            log.warn("listAllJobs: Error retrieving job detail for jobKey: {}", jobKey, e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.info("listAllJobs: Listed all jobs. Total jobs: {}", jobDetails.size());
            log.trace("listAllJobs method ends");

            return jobDetails;

        } catch (SchedulerException e) {
            log.warn("listAllJobs: Error listing jobs: {}", e.getMessage(), e);
            throw new JobListingException("Error listing jobs: " + e.getMessage());
        }
    }
}
