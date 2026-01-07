package org.example.kortex.carts.api.dto;

import org.example.kortex.products.api.dto.ProductDTO;

import java.math.BigDecimal;

public record CartItemDTO(
        Long id,
        CartDTO cart,
        ProductDTO product,
        Integer quantity,
        BigDecimal price
) {
}
