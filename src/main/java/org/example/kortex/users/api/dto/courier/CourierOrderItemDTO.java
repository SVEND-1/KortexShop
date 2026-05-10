package org.example.kortex.users.api.dto.courier;


import java.math.BigDecimal;

public record CourierOrderItemDTO(
        Long id,
        String productName,
        String productImageUrl,
        String productDescription,
        Integer quantity,
        BigDecimal price,
        BigDecimal totalPrice

){
}