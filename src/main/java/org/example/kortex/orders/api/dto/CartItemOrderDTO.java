package org.example.kortex.orders.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemOrderDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
