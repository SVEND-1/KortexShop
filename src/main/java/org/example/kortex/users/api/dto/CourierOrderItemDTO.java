package org.example.kortex.users.api.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourierOrderItemDTO {
    private Long id;
    private String productName;
    private String productCode;
    private String productImageUrl;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String productDescription;
}