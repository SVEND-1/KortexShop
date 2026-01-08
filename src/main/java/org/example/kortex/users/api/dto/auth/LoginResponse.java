package org.example.kortex.users.api.dto.auth;

public record LoginResponse(
        boolean success,
        String message,
        String redirectUrl
) {
}
