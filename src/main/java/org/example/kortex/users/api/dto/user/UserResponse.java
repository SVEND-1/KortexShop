package org.example.kortex.users.api.dto.user;

import org.example.kortex.users.db.Role;

public record UserResponse(
        Long id,
        String email,
        String name,
        Role role,
        String address
){
}
