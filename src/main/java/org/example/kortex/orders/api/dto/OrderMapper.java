package org.example.kortex.orders.api.dto;

import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponseDTO toDto(Order order) {//TODO поменять когда добавиться показ заказов
        if (order == null) {
            return null;
        }

        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getId(),
                itemDTOs,
                order.getOrderDate().toLocalDate(),
                order.getTotalAmount(),
                order.getStatus()
        );


    }

    private OrderItemDTO toItemDto(OrderItem item) {
        return new OrderItemDTO(
                item.getProduct().getName(),
                item.getPrice().doubleValue(),
                item.getQuantity() != null ? item.getQuantity() : 0,
                item.getProduct().getImage()
        );
    }

    public List<OrderResponseDTO> toDtoList(List<Order> orders) {
        return orders.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

}
