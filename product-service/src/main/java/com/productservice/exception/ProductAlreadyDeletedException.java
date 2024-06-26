package com.productservice.exception;

public class ProductAlreadyDeletedException extends RuntimeException{
    public ProductAlreadyDeletedException(String message){ super (message); }
}
