package com.userservice.exception;

public class TakenUsernameException extends RuntimeException {
    public TakenUsernameException(String message) {
        super(message);
    }
    public TakenUsernameException(String message, Throwable cause) {
        super(message, cause);
    }
}
