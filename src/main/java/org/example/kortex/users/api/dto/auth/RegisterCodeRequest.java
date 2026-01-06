package org.example.kortex.users.api.dto.auth;

public record RegisterCodeRequest(
        String email,
        String password,
        String name,
        String code
) {
}
