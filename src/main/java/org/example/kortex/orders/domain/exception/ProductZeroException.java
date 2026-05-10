package org.example.kortex.orders.domain.exception;

public class ProductZeroException extends RuntimeException{
    public ProductZeroException(String message) {
        super(message);
    }
}
