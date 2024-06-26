package com.productservice.exception;

public class ImageAlreadyDeletedException extends RuntimeException{
    public ImageAlreadyDeletedException(String message) {
        super(message);
    }
}
