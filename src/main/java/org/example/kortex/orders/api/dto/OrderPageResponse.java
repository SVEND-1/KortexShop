package org.example.kortex.orders.api.dto;

import org.example.kortex.users.api.dto.courier.CourierOrderDTO;

import java.util.List;

public record OrderPageResponse(
        List<CourierOrderDTO> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
) {
}
