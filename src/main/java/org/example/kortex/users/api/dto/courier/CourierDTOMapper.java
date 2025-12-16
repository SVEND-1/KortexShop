package org.example.kortex.users.api.dto.courier;


import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.products.db.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CourierDTOMapper {//TODO Переделать

    public CourierOrderDTO toCourierOrderDTO(Order order) {
        if (order == null) {
            return null;
        }

        CourierOrderDTO dto = new CourierOrderDTO();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setMessage(order.getMessage());

        // Информация о клиенте
        if (order.getUser() != null) {
            CustomerInfoDTO customerDTO = new CustomerInfoDTO();
            customerDTO.setId(order.getUser().getId());
            customerDTO.setFullName(order.getUser().getName());
            customerDTO.setEmail(order.getUser().getEmail());
            dto.setCustomer(customerDTO);
        }

        // Преобразование элементов заказа
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<CourierOrderItemDTO> courierOrderItemDTOS = order.getOrderItems().stream()
                    .map(this::toOrderItemDTO)
                    .toList();
            dto.setOrderItems(courierOrderItemDTOS);
        }

        return dto;
    }

    public CourierOrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        CourierOrderItemDTO dto = new CourierOrderItemDTO();
        dto.setId(orderItem.getId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());

        // Вычисляем общую стоимость
        if (orderItem.getQuantity() != null && orderItem.getPrice() != null) {
            dto.setTotalPrice(orderItem.getPrice()
                    .multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }

        // Информация о продукте
        if (orderItem.getProduct() != null) {
            Product product = orderItem.getProduct();
            dto.setProductName(product.getName());
            // Проверяем, есть ли эти поля в Product
            if (product.getImage() != null) dto.setProductImageUrl(product.getImage());
            if (product.getDescription() != null) dto.setProductDescription(product.getDescription());
        }

        return dto;
    }

    public Map<String, Object> toPageResponse(Page<Order> orderPage) {
        Map<String, Object> response = new HashMap<>();

        List<CourierOrderDTO> content = orderPage.getContent().stream()
                .map(this::toCourierOrderDTO)
                .toList();

        response.put("content", content);
        response.put("pageNumber", orderPage.getNumber());
        response.put("pageSize", orderPage.getSize());
        response.put("totalElements", orderPage.getTotalElements());
        response.put("totalPages", orderPage.getTotalPages());
        response.put("last", orderPage.isLast());
        response.put("first", orderPage.isFirst());
        response.put("empty", orderPage.isEmpty());

        return response;
    }
}