package org.example.kortex.roleRequest.api;

import org.example.kortex.users.db.Role;
import org.example.kortex.roleRequest.db.RoleRequest;
import org.springframework.web.bind.annotation.RequestParam;

public record RoleRequestFilter(
        Role role,
        RoleRequest.Status status,
        RoleRequest.TypeAction actionType,
        Integer pageSize,
        Integer pageNumber
) {
}
