package org.example.kortex.users.api.dto.user;

import org.example.kortex.users.db.Role;

public record UserDTO(
        Long id,
        String email,
        String name,
        Role role,
        String address
){
}
