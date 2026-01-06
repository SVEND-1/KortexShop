package org.example.kortex.users.api.dto.auth;


public record RegistrationResponse(
        boolean success,
        String message,
        String registrationId
) {
}
