package org.example.kortex.users.api.dto.auth;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record LoginRequest(
        @Email
        @NotNull
        String email,
        @NotNull
        @Size(min = 6, max = 50)
        String password) {

}
