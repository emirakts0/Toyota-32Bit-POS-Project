package com.user.exception;


import jakarta.validation.ConstraintViolationException;
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

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<Object> handleEmployeeNotFoundException(EmployeeNotFoundException e) {
        return buildResponseEntity(e, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler({ NoRolesException.class,
                        IllegalArgumentException.class,
                        ConstraintViolationException.class,
                        InvalidInputException.class })
    public ResponseEntity<Object> handleBadRequestException(RuntimeException e) {
        return buildResponseEntity(e, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler({ TakenUsernameException.class,
                        TakenEmailException.class,
                        EmployeeAlreadyExistsException.class,
                        EmployeeAlreadyDeletedException.class})
    public ResponseEntity<Object> handleConflictException(RuntimeException e) {
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
