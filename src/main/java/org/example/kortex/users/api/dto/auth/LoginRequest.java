package org.example.kortex.users.api.dto.auth;

public record LoginRequest(
        String email,
        String password) {

}
