package com.userservice.exception;


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

    @ExceptionHandler(value = {EmployeeNotFoundException.class,
                            EmployeeAlreadyDeletedException.class,
                            NoRolesException.class,
                            TakenUsernameException.class,
                            TakenEmailException.class,
                            IllegalArgumentException.class,
                            ConstraintViolationException.class,
                            EmployeeAlreadyExistsException.class,
                            InvalidInputException.class})
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
