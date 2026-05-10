package org.example.kortex.orders.domain.mapper;

import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.orders.api.dto.OrderItemDTO;
import org.example.kortex.orders.api.dto.OrderResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface OrderMapper {

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "items", source = "orderItems", qualifiedByName = "mapOrderItems")
    @Mapping(target = "orderDate", source = "orderDate", qualifiedByName = "toLocalDate")
    OrderResponseDTO toDto(Order order);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "items", source = "orderItems", qualifiedByName = "mapOrderItems")
    @Mapping(target = "orderDate", source = "orderDate", qualifiedByName = "toLocalDate")
    List<OrderResponseDTO> toDtoListOrder(List<Order> order);

    List<OrderResponseDTO> toDtoList(List<Order> orders);

    @Mapping(target = "nameProduct", source = "product.name")
    @Mapping(target = "priceProduct", source = "price", qualifiedByName = "toDouble")
    @Mapping(target = "count", source = "quantity")
    @Mapping(target = "image", source = "product.image")
    OrderItemDTO toItemDto(OrderItem item);

    @Named("mapOrderItems")
    default List<OrderItemDTO> mapOrderItems(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return List.of();
        }
        return orderItems.stream()
                .map(this::toItemDto)
                .toList();
    }

    @Named("toLocalDate")
    default LocalDate toLocalDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate();
    }

    @Named("toDouble")
    default Double toDouble(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return 0.0;
        }
        return bigDecimal.doubleValue();
    }

}