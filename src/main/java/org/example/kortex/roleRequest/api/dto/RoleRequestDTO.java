package org.example.kortex.roleRequest.api.dto;

import org.example.kortex.users.api.dto.user.UserDTO;
import org.example.kortex.users.db.Role;
import org.example.kortex.roleRequest.db.RoleRequest;

import java.time.LocalDateTime;

public record RoleRequestDTO(
        Long id,
        UserDTO user,
        Role requestedRole,
        RoleRequest.TypeAction typeAction,
        String message,
        RoleRequest.Status status,
        LocalDateTime createAt
) {
}
