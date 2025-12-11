package org.example.kortex.carts.api.dto;

import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.users.api.dto.UserCartDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartItemDTO toCartItemDto(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setPrice(cartItem.getPrice());
        dto.setQuantity(cartItem.getQuantity());

        // Рассчитываем субтотал
        dto.setSubtotal(cartItem.getPrice().multiply(
                BigDecimal.valueOf(cartItem.getQuantity())
        ));

        // Заполняем информацию о продукте
        if (cartItem.getProduct() != null) {
            dto.setProductId(cartItem.getProduct().getId());
            dto.setProductName(cartItem.getProduct().getName());
            dto.setProductDescription(cartItem.getProduct().getDescription());
            dto.setProductImage(cartItem.getProduct().getImage());

            if (cartItem.getProduct().getCategory() != null) {
                dto.setProductCategory(cartItem.getProduct().getCategory().name());
            }
        }

        return dto;
    }

    public UserCartDTO toUserCartDto(org.example.kortex.users.db.User user) {
        if (user == null) {
            return null;
        }

        UserCartDTO dto = new UserCartDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setRole(user.getRole());
        dto.setAddress(user.getAddress());

        if (user.getCart() != null) {
            dto.setCartId(user.getCart().getId());
        }

        return dto;
    }

    public CartResponseDTO toCartResponseDto(Cart cart) {
        if (cart == null) {
            return null;
        }

        CartResponseDTO dto = new CartResponseDTO();
        dto.setId(cart.getId());

        // Конвертируем CartItems
        if (cart.getCartItems() != null) {
            dto.setCartItems(cart.getCartItems().stream()
                    .map(this::toCartItemDto)
                    .collect(Collectors.toList()));
        }

        // Конвертируем User
        if (cart.getUser() != null) {
            dto.setUser(toUserCartDto(cart.getUser()));
        }

        // Рассчитываем итоги
        dto.calculateTotals();

        return dto;
    }

    public List<CartItemDTO> toListCartItemDto(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(this::toCartItemDto)
                .collect(Collectors.toList());
    }
}
