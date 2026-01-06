package org.example.kortex.users.api.dto.auth;

public record VerifyRegisterRequest(
        String registrationId,
        String code
) {
    public static record RegisterRequest(
            String email,
            String password
    ) {
    }
}
