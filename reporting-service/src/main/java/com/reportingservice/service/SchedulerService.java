package com.reportingservice.service;

import com.reportingservice.dto.JobDetailResponse;
import com.reportingservice.dto.ScheduleRequest;
import com.reportingservice.exception.*;

import java.util.List;

/**
 * Service interface for scheduling, cancelling, and listing scheduled jobs.
 * @author Emir Aktaş
 */
public interface SchedulerService {

    /**
     * Schedules a new job based on the provided schedule request.
     *
     * @param request the schedule request containing job details
     * @throws JobAlreadyExistsException if a job with the same name already exists
     * @throws JobSchedulingException    if there is an error scheduling the job
     * @throws JsonException             if there is an error processing the JSON criteria
     */
    void scheduleJob(ScheduleRequest request);


    /**
     * Cancels a scheduled job by its name.
     *
     * @param jobName the name of the job to cancel
     * @throws JobCancellationException if there is an error cancelling the job or if the job does not exist
     */
    void cancelJob(String jobName);


    /**
     * Lists all scheduled jobs with their details.
     *
     * @return a list of job detail responses
     * @throws JobListingException if there is an error listing the jobs
     */
    List<JobDetailResponse> listAllJobs();
}
