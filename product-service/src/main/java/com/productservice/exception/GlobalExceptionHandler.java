package com.productservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = { ProductNotFoundException.class,
                                ImageNotFoundException.class })
    public ResponseEntity<Object> handleNotFoundExceptions(RuntimeException e) {
        return buildResponseEntity(e, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(value = { ImageProcessingException.class,
                                ProductIsNotDeletedException.class,
                                InvalidInputException.class })
    public ResponseEntity<Object> handleBadRequestExceptions(RuntimeException e) {
        return buildResponseEntity(e, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(value = { ProductAlreadyExistsException.class,
                                ProductAlreadyDeletedException.class,})
    public ResponseEntity<Object> handleConflictExceptions(RuntimeException e) {
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

