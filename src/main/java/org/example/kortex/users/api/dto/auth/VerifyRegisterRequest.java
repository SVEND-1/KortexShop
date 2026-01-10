package org.example.kortex.users.api.dto.auth;

import javax.validation.constraints.NotNull;

public record VerifyRegisterRequest(
        @NotNull
        String registrationId,
        @NotNull
        String code
) {
}
