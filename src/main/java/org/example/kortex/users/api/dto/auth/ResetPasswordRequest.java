package org.example.kortex.users.api.dto.auth;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record ResetPasswordRequest (
        @NotNull
        String resetId,
        @NotNull
        @Size(min = 6, max = 50)
        String newPassword,
        @NotNull
        @Size(min = 6, max = 50)
        String confirmPassword
){
}
