package com.userservice.exception;

public class EmployeeAlreadyDeletedException extends RuntimeException{
    public EmployeeAlreadyDeletedException(String message) {
        super(message);
    }
    public EmployeeAlreadyDeletedException(String message, Throwable cause) {
        super(message, cause);
    }
}
