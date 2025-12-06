package org.example.kortex.orders.api;

public record OrdersSearchCourierFilter(
        Long userId,
        Integer pageSize,
        Integer pageNumber
) {
}
