package org.example.kortex.users.api.dto.auth;

public record PasswordResetResponse(
        boolean success,
        String message,
        String resetId
){
}
