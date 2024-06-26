package com.reportingservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reportingservice.dto.JobDetailResponse;
import com.reportingservice.dto.SaleSearchCriteriaForJob;
import com.reportingservice.dto.ScheduleRequest;
import com.reportingservice.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SchedulerServiceImplTest {

    @Mock
    private SchedulerFactoryBean schedulerFactoryBean;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private Scheduler scheduler;
    @Mock
    private Trigger trigger;
    @Mock
    private JobDetail jobDetail;
    @Mock
    private JobDataMap jobDataMap;

    @InjectMocks
    private SchedulerServiceImpl schedulerService;

    private ScheduleRequest request;
    private SaleSearchCriteriaForJob criteria;

    private void setUpRequest() {
        criteria = new SaleSearchCriteriaForJob();
        request = new ScheduleRequest();
        request.setJobName("testJob");
        request.setEmail("test@example.com");
        request.setCriteria(criteria);
        request.setDaysInterval(1);
    }

    @Test
    void whenScheduleJobWithValidRequest_thenJobScheduledSuccessfully() throws SchedulerException, JsonProcessingException {
        setUpRequest();

        when(schedulerFactoryBean.getScheduler()).thenReturn(scheduler);
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);
        when(objectMapper.writeValueAsString(criteria)).thenReturn("criteriaJson");

        schedulerService.scheduleJob(request);

        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    void whenScheduleJobWithExistingJob_thenThrowJobAlreadyExistsException() throws SchedulerException {
        setUpRequest();

        when(schedulerFactoryBean.getScheduler()).thenReturn(scheduler);
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);

        JobAlreadyExistsException exception = assertThrows(JobAlreadyExistsException.class,
                () -> schedulerService.scheduleJob(request));

        assertEquals("Job with name testJob already exists.", exception.getMessage());
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    void whenScheduleJobWithSchedulerException_thenThrowJobSchedulingException() throws SchedulerException, JsonProcessingException {
        setUpRequest();

        when(schedulerFactoryBean.getScheduler()).thenReturn(scheduler);
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);
        when(objectMapper.writeValueAsString(criteria)).thenReturn("criteriaJson");
        doThrow(new SchedulerException("Scheduler error")).when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));

        JobSchedulingException exception = assertThrows(JobSchedulingException.class,
                () -> schedulerService.scheduleJob(request));

        assertEquals("Error scheduling job: Scheduler error", exception.getMessage());
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    void whenScheduleJobWithJsonProcessingException_thenThrowJsonException() throws JsonProcessingException, SchedulerException {
        setUpRequest();

        when(schedulerFactoryBean.getScheduler()).thenReturn(scheduler);
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);
        when(objectMapper.writeValueAsString(criteria)).thenThrow(new JsonProcessingException("Json error") {});

        JsonException exception = assertThrows(JsonException.class,
                () -> schedulerService.scheduleJob(request));

        assertEquals("Json file could not be processed", exception.getMessage());
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }


    @Test
    void whenCancelJobWithExistingJob_thenJobCancelledSuccessfully() throws SchedulerException {
        String jobName = "testJob";
        JobKey jobKey = new JobKey(jobName);

        when(schedulerFactoryBean.getScheduler()).thenReturn(scheduler);
        when(scheduler.checkExists(jobKey)).thenReturn(true);

        schedulerService.cancelJob(jobName);

        verify(scheduler, times(1)).deleteJob(jobKey);
    }

    @Test
    void whenCancelJobWithNonExistingJob_thenThrowJobCancellationException() throws SchedulerException {
        String jobName = "nonExistingJob";
        JobKey jobKey = new JobKey(jobName);

        when(schedulerFactoryBean.getScheduler()).thenReturn(scheduler);
        when(scheduler.checkExists(jobKey)).thenReturn(false);

        JobCancellationException exception = assertThrows(JobCancellationException.class,
                () -> schedulerService.cancelJob(jobName));

        assertEquals("Job with name " + jobName + " does not exists.", exception.getMessage());
        verify(scheduler, never()).deleteJob(jobKey);
    }

    @Test
    void whenCancelJobWithSchedulerException_thenThrowJobCancellationException() throws SchedulerException {
        String jobName = "testJob";
        JobKey jobKey = new JobKey(jobName);

        when(schedulerFactoryBean.getScheduler()).thenReturn(scheduler);
        when(scheduler.checkExists(jobKey)).thenReturn(true);
        doThrow(new SchedulerException("Scheduler error")).when(scheduler).deleteJob(jobKey);

        JobCancellationException exception = assertThrows(JobCancellationException.class,
                () -> schedulerService.cancelJob(jobName));

        assertEquals("Error cancelling job: Scheduler error", exception.getMessage());
        verify(scheduler, times(1)).deleteJob(jobKey);
    }


    @Test
    void whenListAllJobs_thenReturnJobDetails() throws SchedulerException, JsonProcessingException {
        JobKey jobKey = new JobKey("testJob");
        SaleSearchCriteriaForJob criteria = new SaleSearchCriteriaForJob();
        String criteriaJson = "{\"criteria\":\"test\"}";

        when(schedulerFactoryBean.getScheduler()).thenReturn(scheduler);
        when(scheduler.getJobKeys(GroupMatcher.anyGroup())).thenReturn(Collections.singleton(jobKey));
        when(scheduler.getJobDetail(jobKey)).thenReturn(jobDetail);
        when(scheduler.getTriggersOfJob(jobKey)).thenAnswer(invocation -> List.of(trigger));
        when(trigger.getNextFireTime()).thenReturn(null);
        when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
        when(jobDataMap.getString("email")).thenReturn("test@example.com");
        when(jobDataMap.getString("criteria")).thenReturn(criteriaJson);
        when(objectMapper.readValue(criteriaJson, SaleSearchCriteriaForJob.class)).thenReturn(criteria);

        List<JobDetailResponse> jobDetails = schedulerService.listAllJobs();

        assertNotNull(jobDetails);
        assertEquals(1, jobDetails.size());
        JobDetailResponse response = jobDetails.get(0);
        assertEquals("testJob", response.getJobName());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(criteria, response.getCriteria());
        assertEquals("No next fire time", response.getNextFireTime());
    }

    @Test
    void whenListAllJobsWithSchedulerException_thenThrowJobListingException() throws SchedulerException {
        when(schedulerFactoryBean.getScheduler()).thenReturn(scheduler);
        when(scheduler.getJobKeys(GroupMatcher.anyGroup())).thenThrow(new SchedulerException("Scheduler error"));

        JobListingException exception = assertThrows(JobListingException.class,
                () -> schedulerService.listAllJobs());

        assertEquals("Error listing jobs: Scheduler error", exception.getMessage());
    }

    @Test
    void whenListAllJobsWithInnerSchedulerException_thenHandleGracefully() throws SchedulerException {
        JobKey jobKey = new JobKey("testJob");

        when(schedulerFactoryBean.getScheduler()).thenReturn(scheduler);
        when(scheduler.getJobKeys(GroupMatcher.anyGroup())).thenReturn(Collections.singleton(jobKey));
        when(scheduler.getJobDetail(jobKey)).thenThrow(new SchedulerException("JobDetail error"));

        List<JobDetailResponse> jobDetails = schedulerService.listAllJobs();

        assertNotNull(jobDetails);
        assertTrue(jobDetails.isEmpty(), "JobDetails should be empty due to error in job detail retrieval");
    }
}