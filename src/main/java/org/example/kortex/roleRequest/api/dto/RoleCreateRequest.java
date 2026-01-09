package org.example.kortex.roleRequest.api.dto;

import org.example.kortex.roleRequest.db.RoleRequest;
import org.example.kortex.users.db.Role;

public record RoleCreateRequest(
        Role requestedRole,
        RoleRequest.TypeAction typeAction,
        String message
) {
}
