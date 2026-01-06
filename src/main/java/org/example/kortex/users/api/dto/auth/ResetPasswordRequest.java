package org.example.kortex.users.api.dto.auth;

public record ResetPasswordRequest (
        String resetId,
        String newPassword,
        String confirmPassword
){
}
