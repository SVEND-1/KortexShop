package org.example.kortex.carts.api.dto;

import org.example.kortex.users.api.dto.user.UserDTO;

import java.math.BigDecimal;
import java.util.List;

public record CartDTO(
        Long id,
        Integer itemsCount,
        BigDecimal totalPrice,
        Integer totalQuantity
) {
}
