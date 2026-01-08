package org.example.kortex.orders.api.dto;

import org.example.kortex.carts.api.dto.CartItemResponse;
import org.example.kortex.users.api.dto.user.UserResponse;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderPageDTO(
        List<CartItemResponse> cartItems,
        BigDecimal totalPrice,
        Integer totalItems,
        UserResponse user
) {
}
