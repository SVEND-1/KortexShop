package org.example.kortex.carts.api.dto;

import lombok.Data;
import org.example.kortex.users.api.dto.UserCartDTO;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponseDTO {
    private Long id;
    private BigDecimal totalPrice;
    private Integer totalItems;
    private List<CartItemDTO> cartItems;
    private UserCartDTO user;


    public void calculateTotals() {
        if (cartItems != null) {
            this.totalItems = cartItems.stream()
                    .mapToInt(CartItemDTO::getQuantity)
                    .sum();

            this.totalPrice = cartItems.stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }
}
