package org.example.kortex.carts.api.dto;

import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.users.api.dto.UserCartDTO;
import org.example.kortex.users.db.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartItemDTO toCartItemDto(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());

        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
            dto.setProductName(item.getProduct().getName());
            dto.setImage(item.getProduct().getImage());
        }

        return dto;
    }

    public CartResponseDTO toCartResponse(Cart cart) {
        CartResponseDTO dto = new CartResponseDTO();

        dto.setItems(
                cart.getCartItems().stream()
                        .map(this::toCartItemDto)
                        .collect(Collectors.toList())
        );

        BigDecimal total = cart.getCartItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        dto.setTotal(total);

        return dto;
    }

    public List<CartItemDTO> toListCartItemDto(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(this::toCartItemDto)
                .collect(Collectors.toList());
    }
    public UserCartDTO toUserCartDto(User user) {
        if (user == null) return null;

        UserCartDTO dto = new UserCartDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setAddress(user.getAddress());

        return dto;
    }
}
