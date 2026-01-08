package org.example.kortex.roleRequest.api;

import org.example.kortex.users.db.Role;
import org.example.kortex.roleRequest.db.RoleRequest;

public record RoleRequestFilter(
        Role role,
        RoleRequest.Status status,
        RoleRequest.TypeAction typeAction,
        Integer pageSize,
        Integer pageNumber
) {
}
