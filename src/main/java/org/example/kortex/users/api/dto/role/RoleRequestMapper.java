package org.example.kortex.users.api.dto.role;

import org.example.kortex.users.db.RoleRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RoleRequestMapper {
    public RoleRequestResponse toDto(RoleRequest roleRequest) {
        if (roleRequest == null) {
            return null;
        }

        RoleRequestResponse dto = new RoleRequestResponse(
                roleRequest.getId(),
                roleRequest.getStatus(),
                roleRequest.getTypeAction(),
                roleRequest.getMessage(),
                roleRequest.getCreatedAt(),
                roleRequest.getUser().getId(),
                roleRequest.getUser().getName(),
                roleRequest.getUser().getEmail()
        );

        return dto;
    }


    public List<RoleRequestResponse> toDtoList(List<RoleRequest> roleRequests) {
        return roleRequests.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Map<String, Object> toPageResponse(Page<RoleRequest> roleRequests) {
        Map<String, Object> response = new HashMap<>();

        response.put("content", toDtoList(roleRequests.getContent()));

        response.put("page", roleRequests.getNumber());
        response.put("size", roleRequests.getSize());
        response.put("totalElements", roleRequests.getTotalElements());
        response.put("totalPages", roleRequests.getTotalPages());
        response.put("first", roleRequests.isFirst());
        response.put("last", roleRequests.isLast());
        response.put("empty", roleRequests.isEmpty());

        return response;
    }
}
