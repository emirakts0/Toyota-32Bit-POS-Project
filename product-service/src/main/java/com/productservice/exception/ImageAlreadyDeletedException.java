package com.productservice.exception;

public class ImageAlreadyDeletedException extends RuntimeException{

    public ImageAlreadyDeletedException(String message) {
        super(message);
    }
    public ImageAlreadyDeletedException(String message, Throwable cause){ super(message, cause); }
}
