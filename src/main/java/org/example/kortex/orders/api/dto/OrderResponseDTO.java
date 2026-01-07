package org.example.kortex.orders.api.dto;

import lombok.Data;
import org.example.kortex.orders.db.Order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Data
public class OrderResponseDTO {
    private Long orderId;
    private List<OrderItemDTOO> items;
    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private Order.OrderStatus status;
}
