package org.example.kortex.users.api.dto.courier;

import org.example.kortex.orders.db.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


public record CourierOrderDTO(
    Long id,
    LocalDateTime orderDate,
    Order.OrderStatus status,
    BigDecimal totalAmount,
    String shippingAddress,
    String message,
    CustomerInfoDTO customer,
    List<CourierOrderItemDTO> orderItems
){

}