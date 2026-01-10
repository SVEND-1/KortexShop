package org.example.kortex.roleRequest.api.dto;

import org.example.kortex.roleRequest.db.RoleRequest;
import org.example.kortex.users.db.Role;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record RoleCreateRequest(
        @NotNull
        Role requestedRole,
        @NotNull
        RoleRequest.TypeAction typeAction,
        @NotNull
        @Size(min = 20, max = 500)
        String message
) {
}
