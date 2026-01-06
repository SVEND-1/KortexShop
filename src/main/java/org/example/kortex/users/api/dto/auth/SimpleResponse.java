package org.example.kortex.users.api.dto.auth;

public record SimpleResponse(
        boolean success,
        String message
) {
}
