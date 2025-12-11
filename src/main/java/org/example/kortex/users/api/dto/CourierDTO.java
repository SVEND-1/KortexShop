package org.example.kortex.users.api.dto;

import lombok.Data;
import org.example.kortex.orders.api.dto.OrderItemDTO;
import org.example.kortex.orders.db.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourierDTO {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String vehicleType; // Тип транспорта
    private String vehicleNumber; // Номер транспорта
    private Double rating; // Рейтинг курьера
    private Integer completedOrders; // Количество выполненных заказов
    private Boolean isActive;
}
