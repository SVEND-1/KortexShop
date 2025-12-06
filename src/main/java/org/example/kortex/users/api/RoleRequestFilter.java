package org.example.kortex.users.api;

import org.example.kortex.users.db.RoleRequest;
import org.example.kortex.users.db.User;

public record RoleRequestFilter(
        User.Role role,
        RoleRequest.Status status,
        Integer pageSize,
        Integer pageNumber
) {
}
