package org.example.kortex.carts.domain.mapper;

import org.example.kortex.carts.api.dto.CartItemResponse;
import org.example.kortex.carts.api.dto.CartResponse;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface CartMapper {

    @Mapping(target = "productId",source = "product.id")
    @Mapping(target = "productName",source = "product.name")
    @Mapping(target = "image",source = "product.image")
    CartItemResponse toCartItemDto(CartItem item);

    List<CartItemResponse> toListCartItemDto(List<CartItem> cartItems);

    @Mapping(target = "items", source = "cartItems")
    @Mapping(target = "total", source = "cart", qualifiedByName = "calculateTotal")
    CartResponse toCartResponse(Cart cart);

    @Named("calculateTotal")
    default BigDecimal calculateTotal(Cart cart) {
        return cart.totalPrice();
    }
}
