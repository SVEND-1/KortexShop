package org.example.kortex.carts.api.dto;

import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartItemResponse toCartItemDto(CartItem item) {
        CartItemResponse dto = new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getImage(),
                item.getPrice(),
                item.getQuantity()
        );

        return dto;
    }

    public CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items =                cart.getCartItems().stream()
                .map(this::toCartItemDto)
                .collect(Collectors.toList());
        BigDecimal total = cart.getCartItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        CartResponse dto = new CartResponse(
                items,total
        );
        return dto;
    }

    public List<CartItemResponse> toListCartItemDto(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(this::toCartItemDto)
                .collect(Collectors.toList());
    }
}
