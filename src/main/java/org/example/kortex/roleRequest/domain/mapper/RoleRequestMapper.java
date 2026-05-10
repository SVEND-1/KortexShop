package org.example.kortex.roleRequest.domain.mapper;

import org.example.kortex.roleRequest.api.dto.RolePageResponse;
import org.example.kortex.roleRequest.api.dto.RoleRequestResponse;
import org.example.kortex.roleRequest.db.RoleRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper
public interface RoleRequestMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "email", source = "user.email")
    RoleRequestResponse toDto(RoleRequest roleRequest);


    List<RoleRequestResponse> toDtoList(List<RoleRequest> roleRequests);

    default RolePageResponse toPageResponse(Page<RoleRequest> roleRequests) {
        if (roleRequests == null) {
            return null;
        }

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

