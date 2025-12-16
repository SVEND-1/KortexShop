package org.example.kortex.users.api.dto.courier;

import lombok.Data;
import org.example.kortex.orders.db.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourierOrderDTO {
    private Long id;
    private LocalDateTime orderDate;
    private Order.OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String message;
    private CustomerInfoDTO customer;
    private List<CourierOrderItemDTO> orderItems;

}