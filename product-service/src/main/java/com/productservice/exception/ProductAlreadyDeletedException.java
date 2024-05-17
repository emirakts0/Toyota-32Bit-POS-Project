package com.productservice.exception;

public class ProductAlreadyDeletedException extends RuntimeException{

    public ProductAlreadyDeletedException(String message){ super (message); }
    public ProductAlreadyDeletedException(String message, Throwable cause){ super(message, cause); }
}
