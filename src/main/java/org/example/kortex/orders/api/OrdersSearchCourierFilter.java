package org.example.kortex.orders.api;


public record OrdersSearchCourierFilter(
        Long courierId,
        Integer pageSize,
        Integer pageNumber
) {
}
