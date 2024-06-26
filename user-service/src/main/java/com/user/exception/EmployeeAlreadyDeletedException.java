package com.user.exception;

public class EmployeeAlreadyDeletedException extends RuntimeException{
    public EmployeeAlreadyDeletedException(String message) {
        super(message);
    }
}
