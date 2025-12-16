package org.example.kortex.orders.api.dto;

public record UserOrderResponse(
        Long id,
        String email,
        String name,
        String address) {
}

