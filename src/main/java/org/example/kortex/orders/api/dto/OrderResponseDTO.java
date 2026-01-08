package org.example.kortex.orders.api.dto;


import org.example.kortex.orders.db.Order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


public record OrderResponseDTO(
    Long orderId,
    List<OrderItemDTO> items,
    LocalDate orderDate,
    BigDecimal totalAmount,
    Order.OrderStatus status
    ){
}
