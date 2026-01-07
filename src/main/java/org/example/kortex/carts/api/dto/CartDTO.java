package org.example.kortex.carts.api.dto;

import org.example.kortex.users.api.dto.user.UserDTO;

import java.util.List;

public record CartDTO(
    Long id,
    UserDTO user,
    List<CartItemDTO> cartItem
) {
}
