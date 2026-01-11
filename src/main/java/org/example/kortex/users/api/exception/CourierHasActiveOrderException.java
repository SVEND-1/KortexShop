package org.example.kortex.users.api.exception;

public class CourierHasActiveOrderException extends IllegalStateException{
    public CourierHasActiveOrderException(String message) {
        super(message);
    }
}
