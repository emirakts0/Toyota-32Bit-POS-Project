package com.reportingservice.exception;

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
                                InvalidInputException.class,
                                ReceiptDataIsNullException.class,
                                SaleNotFoundException.class,
                                JsonException.class})
    public ResponseEntity<Object> responseEntity(RuntimeException e){

        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                e.getMessage(),
                badRequest,
                LocalDateTime.now()
        );

        log.error("Exception: {} - Message: {}", e.getClass().getSimpleName(), e.getMessage(), e);

        return ResponseEntity
                .status(badRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .body(exceptionResponse);
    }
}
