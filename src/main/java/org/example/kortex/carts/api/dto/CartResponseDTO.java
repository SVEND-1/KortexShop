package org.example.kortex.carts.api.dto;

import lombok.Data;
import org.example.kortex.users.api.dto.UserCartDTO;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponseDTO {
    private List<CartItemDTO> items;
    private BigDecimal total;
}
