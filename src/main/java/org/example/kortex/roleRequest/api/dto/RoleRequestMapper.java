package org.example.kortex.roleRequest.api.dto;

import org.example.kortex.roleRequest.db.RoleRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoleRequestMapper {
    public RoleRequestResponse toDto(RoleRequest roleRequest) {
        if (roleRequest == null) {
            return null;
        }

        return new RoleRequestResponse(
                roleRequest.getId(),
                roleRequest.getStatus(),
                roleRequest.getTypeAction(),
                roleRequest.getMessage(),
                roleRequest.getCreatedAt(),
                roleRequest.getUser().getId(),
                roleRequest.getUser().getName(),
                roleRequest.getUser().getEmail()
        );
    }


    public List<RoleRequestResponse> toDtoList(List<RoleRequest> roleRequests) {
        return roleRequests.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public RolePageResponse toPageResponse(Page<RoleRequest> roleRequests) {

        return new RolePageResponse(
                toDtoList(roleRequests.getContent()),
                roleRequests.getNumber(),
                roleRequests.getSize(),
                roleRequests.getTotalElements(),
                roleRequests.getTotalPages(),
                roleRequests.isFirst(),
                roleRequests.isLast(),
                roleRequests.isEmpty()
        );
    }
}
