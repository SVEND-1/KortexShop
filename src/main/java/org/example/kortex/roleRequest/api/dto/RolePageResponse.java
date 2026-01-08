package org.example.kortex.roleRequest.api.dto;

import java.util.List;

public record RolePageResponse(
        List<RoleRequestResponse> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
) {
}
