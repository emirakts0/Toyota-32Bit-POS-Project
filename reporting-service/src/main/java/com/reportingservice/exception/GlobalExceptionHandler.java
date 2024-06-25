package com.reportingservice.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = { NotFoundException.class,
                                SaleNotFoundException.class })
    public ResponseEntity<Object> handleNotFoundExceptions(RuntimeException e) {
        return buildResponseEntity(e, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(value = { InvalidInputException.class,
                                ReceiptDataIsNullException.class,
                                JobCancellationException.class,
                                JobSchedulingException.class,
                                JobListingException.class,
                                ConstraintViolationException.class,
                                JsonException.class })
    public ResponseEntity<Object> handleBadRequestExceptions(RuntimeException e) {
        return buildResponseEntity(e, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(JobAlreadyExistsException.class)
    public ResponseEntity<Object> handleConflictExceptions(JobAlreadyExistsException e) {
        return buildResponseEntity(e, HttpStatus.CONFLICT);
    }


    private ResponseEntity<Object> buildResponseEntity(RuntimeException e, HttpStatus status) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                e.getMessage(),
                status,
                LocalDateTime.now()
        );

        log.error("Exception: {} - Message: {}", e.getClass().getSimpleName(), e.getMessage(), e);

        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(exceptionResponse);
    }
}
