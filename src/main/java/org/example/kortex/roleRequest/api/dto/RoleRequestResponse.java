package org.example.kortex.roleRequest.api.dto;


import org.example.kortex.roleRequest.db.RoleRequest;

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
