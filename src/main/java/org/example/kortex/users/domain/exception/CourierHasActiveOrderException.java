package org.example.kortex.users.domain.exception;

public class CourierHasActiveOrderException extends IllegalStateException{
    public CourierHasActiveOrderException(String message) {
        super(message);
    }
}
