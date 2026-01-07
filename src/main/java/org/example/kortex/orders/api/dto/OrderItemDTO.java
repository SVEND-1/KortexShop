package org.example.kortex.orders.api.dto;

import org.example.kortex.products.api.dto.ProductDTO;

import java.math.BigDecimal;

public record OrderItemDTO(
        Long id,
        ProductDTO product,
        Integer quantity,
        BigDecimal price
) {
}
