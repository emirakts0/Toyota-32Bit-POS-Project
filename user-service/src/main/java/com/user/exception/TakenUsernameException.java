package com.user.exception;

public class TakenUsernameException extends RuntimeException {
    public TakenUsernameException(String message) {
        super(message);
    }
}
