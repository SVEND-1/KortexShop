package org.example.kortex.orders.api.exception;

public class ProductZeroException extends RuntimeException{
    public ProductZeroException(String message) {
        super(message);
    }
}
