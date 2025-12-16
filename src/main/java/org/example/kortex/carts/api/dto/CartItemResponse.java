package org.example.kortex.carts.api.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        String image,
        BigDecimal price,
        Integer quantity
) {

}