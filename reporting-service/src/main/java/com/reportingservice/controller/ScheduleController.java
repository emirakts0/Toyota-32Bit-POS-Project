package com.reportingservice.controller;

import com.reportingservice.dto.JobDetailResponse;
import com.reportingservice.dto.ScheduleRequest;
import com.reportingservice.service.SchedulerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("report/schedule")
public class ScheduleController {

    private final SchedulerService schedulerService;


    @PostMapping
    public ResponseEntity<String> scheduleJob(@RequestBody @Valid ScheduleRequest request) {
        log.trace("scheduleJob endpoint called with request: {}", request);

        schedulerService.scheduleJob(request);
        return ResponseEntity.ok("Job scheduled successfully: " + request.getJobName());
    }


    @GetMapping("/list")
    public ResponseEntity<List<JobDetailResponse>> listAllJobs() {
        log.trace("listAllJobs endpoint called");

        List<JobDetailResponse> jobNames = schedulerService.listAllJobs();
        return ResponseEntity.ok(jobNames);
    }


    @DeleteMapping("/cancel/{jobName}")
    public ResponseEntity<String> cancelJob(@PathVariable @NotBlank String jobName) {
        log.trace("cancelJob endpoint called for jobName: {}", jobName);

        schedulerService.cancelJob(jobName);
        return ResponseEntity.ok("Job cancelled successfully: " + jobName);
    }
}
