package org.example.kortex.users.api.dto;

import org.example.kortex.products.api.dto.ProductResponseDTO;
import org.example.kortex.products.db.Product;
import org.example.kortex.users.db.RoleRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // Или возвращайте Map с полной информацией о пагинации
    public Map<String, Object> toPageResponse(Page<RoleRequest> roleRequests) {
        Map<String, Object> response = new HashMap<>();

        // Контент
        response.put("content", toDtoList(roleRequests.getContent()));
        // Информация о пагинации
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
