package org.example.kortex.users.api.dto.auth;

public record VerifyRegisterRequest(
        String registrationId,
        String code
) {
}
