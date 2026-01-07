package org.example.kortex.orders.api.dto;

import org.example.kortex.orders.db.Order;
import org.example.kortex.users.api.dto.user.UserDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO(
        Long id,
        UserDTO user,
        UserDTO courier,
        Order.OrderStatus status,
        LocalDateTime orderDate,
        String shippingAddress,
        String message,
        BigDecimal totalAmount
) {
}
