package org.example.kortex.orders.api.dto;


public record OrdersSearchCourierFilter(
        Integer pageSize,
        Integer pageNumber
) {
}
