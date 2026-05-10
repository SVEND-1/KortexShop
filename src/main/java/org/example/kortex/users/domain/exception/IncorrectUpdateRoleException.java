package org.example.kortex.users.domain.exception;

public class IncorrectUpdateRoleException extends RuntimeException{
    public IncorrectUpdateRoleException(String message) {
        super(message);
    }
}
