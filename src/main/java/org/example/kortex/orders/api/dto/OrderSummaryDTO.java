package org.example.kortex.orders.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSummaryDTO {
    private BigDecimal subtotal; // Сумма товаров
    private BigDecimal deliveryCost; // Стоимость доставки
    private BigDecimal discount; // Скидка
    private BigDecimal total; // Итоговая сумма
    private Double totalItems; // Общее количество товаров
}
