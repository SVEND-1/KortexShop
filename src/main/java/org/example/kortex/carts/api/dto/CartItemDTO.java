package org.example.kortex.carts.api.dto;

import org.example.kortex.orders.db.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CartItemDTO(
        Long id,
        Order.OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime orderDate,
        String shippingAddress,
        Long productId
) {
}
