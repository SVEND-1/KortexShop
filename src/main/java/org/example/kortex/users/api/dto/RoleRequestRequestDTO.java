package org.example.kortex.users.api.dto;

import lombok.Data;
import org.example.kortex.users.db.RoleRequest;

import java.time.LocalDateTime;

@Data
public class RoleRequestRequestDTO {
    private Long Id;
    private RoleRequest.Status status;
    private RoleRequest.TypeAction typeAction;
    private String message;
    private LocalDateTime createdAt;
    private Long userId;
    private String name;
    private String email;
}
