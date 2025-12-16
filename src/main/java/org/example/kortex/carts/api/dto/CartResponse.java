package org.example.kortex.carts.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        BigDecimal total
) {
}
