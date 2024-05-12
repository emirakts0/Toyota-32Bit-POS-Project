package com.userservice.exception;

public class NoRolesException extends RuntimeException{
    public NoRolesException(String message) {
        super(message);
    }
    public NoRolesException(String message, Throwable cause) {
        super(message, cause);
    }
}
