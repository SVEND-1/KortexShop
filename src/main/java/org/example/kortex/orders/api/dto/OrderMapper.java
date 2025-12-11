package org.example.kortex.orders.api.dto;

import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.products.api.dto.ProductResponseDTO;
import org.example.kortex.products.db.Product;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponseDTO toDto(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();

        if (order == null) {
            return null;
        }

        dto.setOrderId(order.getId());
        dto.setOrderDate(order.getOrderDate().toLocalDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());

        // Преобразуем все товары в заказе
        if (order.getOrderItems() != null) {
            List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                    .map(this::toItemDto)
                    .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }

        return dto;
    }



    private OrderItemDTO toItemDto(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();

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


    public List<OrderItemDTO> toDtoItemsList(List<OrderItem> ordersItem) {
        if (ordersItem == null) {
            return Collections.emptyList();
        }

        return ordersItem.stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> toDtoList(List<Order> orders) {
        if (orders == null) {
            return Collections.emptyList();
        }

        return orders.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
