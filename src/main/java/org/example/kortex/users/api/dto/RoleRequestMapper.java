package org.example.kortex.users.api.dto;

import org.example.kortex.products.api.dto.ProductResponseDTO;
import org.example.kortex.products.db.Product;
import org.example.kortex.users.db.RoleRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoleRequestMapper {
    public RoleRequestRequestDTO toDto(RoleRequest roleRequest) {
        if (roleRequest == null) {
            return null;
        }

        RoleRequestRequestDTO dto = new RoleRequestRequestDTO();
        dto.setId(roleRequest.getId());
        dto.setMessage(roleRequest.getMessage());
        dto.setStatus(roleRequest.getStatus());
        dto.setCreatedAt(roleRequest.getCreatedAt());
        dto.setTypeAction(roleRequest.getTypeAction());
        dto.setUserId(roleRequest.getUser().getId());
        dto.setName(roleRequest.getUser().getName());
        dto.setEmail(roleRequest.getUser().getEmail());


        return dto;
    }



    public List<RoleRequestRequestDTO> toDtoList(List<RoleRequest> roleRequests) {
        return roleRequests.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
