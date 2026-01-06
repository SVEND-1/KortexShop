package org.example.kortex.users.api.dto.auth;


import java.io.Serializable;

public record LoginResponse(
        boolean success,
        String message,
        String redirectUrl
) {
}
