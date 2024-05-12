package com.userservice.exception;

public class TakenEmailException extends RuntimeException {
    public TakenEmailException(String message) {
        super(message);
    }
    public TakenEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
