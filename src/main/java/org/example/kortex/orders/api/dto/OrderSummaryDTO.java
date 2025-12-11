package org.example.kortex.orders.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSummaryDTO {
    private BigDecimal subtotal;
    private BigDecimal deliveryCost;
    private BigDecimal discount;
    private BigDecimal total;
    private Double totalItems;
}
