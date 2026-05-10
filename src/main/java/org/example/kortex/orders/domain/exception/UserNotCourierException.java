package org.example.kortex.orders.domain.exception;

public class UserNotCourierException extends IllegalArgumentException{
    public UserNotCourierException(String message) {
        super(message);
    }
}
