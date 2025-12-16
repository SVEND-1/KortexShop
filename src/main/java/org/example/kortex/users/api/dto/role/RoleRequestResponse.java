package org.example.kortex.users.api.dto.role;


import org.example.kortex.users.db.RoleRequest;

import java.time.LocalDateTime;

public record RoleRequestResponse(
        Long id,
        RoleRequest.Status status,
        RoleRequest.TypeAction typeAction ,
        String message,
        LocalDateTime createdAt,
        Long userId,
        String name,
        String email
){
}
