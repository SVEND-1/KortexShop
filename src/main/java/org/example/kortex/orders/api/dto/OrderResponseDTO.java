package org.example.kortex.orders.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.products.db.Product;
import org.example.kortex.users.db.User;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public class OrderResponseDTO {
    private Long orderId;
    private List<OrderItemDTO> items;
    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private Order.OrderStatus status;
}
