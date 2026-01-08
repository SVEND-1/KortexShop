package org.example.kortex.users.api.dto.courier;


import org.example.kortex.orders.api.dto.OrderPageResponse;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.products.db.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CourierDTOMapper {//TODO Переделать

    public CourierOrderDTO toCourierOrderDTO(Order order) {
        if (order == null) {
            return null;
        }

        CustomerInfoDTO customerDTO = new CustomerInfoDTO(
                order.getUser().getId(),
                order.getUser().getName(),
                order.getUser().getEmail()
        );

        List<CourierOrderItemDTO> courierOrderItemDTOS = order.getOrderItems().stream()
                .map(this::toOrderItemDTO)
                .toList();

        return new CourierOrderDTO(
                order.getId(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getMessage(),
                customerDTO,
                courierOrderItemDTOS
        );

    }

    public CourierOrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        Product product = orderItem.getProduct();

        return new CourierOrderItemDTO(
                orderItem.getId(),
                product.getName(),
                null,
                product.getImage(),
                product.getDescription(),
                orderItem.getQuantity(),
                orderItem.getPrice(),
                orderItem.getPrice()
                        .multiply(BigDecimal.valueOf(orderItem.getQuantity()))
        );
    }

    public OrderPageResponse toPageResponse(Page<Order> orderPage) {

        List<CourierOrderDTO> content = orderPage.getContent().stream()
                .map(this::toCourierOrderDTO)
                .toList();

        return new OrderPageResponse(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isFirst(),
                orderPage.isLast(),
                orderPage.isEmpty()
        );
    }
}