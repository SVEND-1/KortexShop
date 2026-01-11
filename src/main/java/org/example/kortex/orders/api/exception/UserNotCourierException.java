package org.example.kortex.orders.api.exception;

public class UserNotCourierException extends IllegalArgumentException{
    public UserNotCourierException(String message) {
        super(message);
    }
}
