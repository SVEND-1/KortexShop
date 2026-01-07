package org.example.kortex.orders.api.dto;

import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.users.db.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponseDTO toDto(Order order) {//TODO поменять когда добавиться показ заказов
        OrderResponseDTO dto = new OrderResponseDTO();

        if (order == null) {
            return null;
        }

        dto.setOrderId(order.getId());
        dto.setOrderDate(order.getOrderDate().toLocalDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());

        if (order.getOrderItems() != null) {
            List<OrderItemDTOO> itemDTOs = order.getOrderItems().stream()
                    .map(this::toItemDto)
                    .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }

        return dto;
    }

    private OrderItemDTOO toItemDto(OrderItem item) {
        OrderItemDTOO dto = new OrderItemDTOO();

        if (item.getProduct() != null) {
            dto.setNameProduct(item.getProduct().getName());
            dto.setImage(item.getProduct().getImage());
        }

        if (item.getPrice() != null) {
            dto.setPriceProduct(item.getPrice().doubleValue());
        }

        dto.setCount(item.getQuantity() != null ? item.getQuantity() : 0);

        return dto;
    }

    public List<OrderResponseDTO> toDtoList(List<Order> orders) {
        if (orders == null) {
            return Collections.emptyList();
        }

        return orders.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserOrderResponse toUserOrderDto(User user) {
        if (user == null) return null;

        UserOrderResponse dto = new UserOrderResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAddress()
        );
        return dto;
    }
}
