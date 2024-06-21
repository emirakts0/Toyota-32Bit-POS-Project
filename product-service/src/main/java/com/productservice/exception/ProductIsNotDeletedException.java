package com.productservice.exception;

public class ProductIsNotDeletedException extends RuntimeException{
    public ProductIsNotDeletedException(String message){ super (message); }
}
