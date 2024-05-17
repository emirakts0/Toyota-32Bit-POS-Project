package com.productservice.exception;

public class ProductIsNotDeletedException extends RuntimeException{

    public ProductIsNotDeletedException(String message){ super (message); }
    public ProductIsNotDeletedException(String message, Throwable cause){ super(message, cause); }
}
