package org.example.kortex.orders.api.dto;

import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.users.db.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderCreateMapper {

    public OrderCreatePageDTO toOrderCreatePageDTO(User user, Cart cart) {
        if (user == null || cart == null) {
            return null;
        }

        OrderCreatePageDTO dto = new OrderCreatePageDTO();

        // 1. Данные пользователя
        dto.setUserInfo(toUserInfoDTO(user));

        // 2. Товары в корзине
        dto.setCartItems(toCartItemOrderDTOList(cart.getCartItems()));

        // 3. Итоговая информация
        dto.setSummary(calculateOrderSummary(cart, user));

        // 4. Способы оплаты
        dto.setPaymentMethods(getAvailablePaymentMethods());

        return dto;
    }

    private UserInfoDTO toUserInfoDTO(User user) {
        UserInfoDTO dto = new UserInfoDTO();

        // Если у вас есть отдельное поле fullName, используйте его
        // Иначе можно скомбинировать name + surname

        dto.setFullName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setAddress(user.getAddress());

        return dto;
    }

    private List<CartItemOrderDTO> toCartItemOrderDTOList(List<CartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return Collections.emptyList();
        }

        return cartItems.stream()
                .map(this::toCartItemOrderDTO)
                .collect(Collectors.toList());
    }

    private CartItemOrderDTO toCartItemOrderDTO(CartItem cartItem) {
        CartItemOrderDTO dto = new CartItemOrderDTO();

        dto.setId(cartItem.getId());
        dto.setQuantity(cartItem.getQuantity());
        dto.setPrice(cartItem.getPrice());
        dto.setSubtotal(cartItem.getPrice().multiply(
                BigDecimal.valueOf(cartItem.getQuantity())
        ));

        if (cartItem.getProduct() != null) {
            dto.setProductId(cartItem.getProduct().getId());
            dto.setProductName(cartItem.getProduct().getName());
            dto.setProductImage(cartItem.getProduct().getImage());
        }

        return dto;
    }

    private OrderSummaryDTO calculateOrderSummary(Cart cart, User user) {
        OrderSummaryDTO summary = new OrderSummaryDTO();

        BigDecimal subtotal = cart.totalPrice();

        summary.setTotalItems(Double.parseDouble(String.valueOf(subtotal)));

        return summary;
    }

    private List<PaymentMethodDTO> getAvailablePaymentMethods() {
        List<PaymentMethodDTO> methods = new ArrayList<>();

        PaymentMethodDTO card = new PaymentMethodDTO();
        card.setId("card");
        card.setName("Банковская карта");
        card.setDescription("Оплата картой онлайн");
        methods.add(card);

        PaymentMethodDTO cash = new PaymentMethodDTO();
        cash.setId("cash");
        cash.setName("Наличные");
        cash.setDescription("Оплата при получении");
        methods.add(cash);

        PaymentMethodDTO sbp = new PaymentMethodDTO();
        sbp.setId("sbp");
        sbp.setName("СБП");
        sbp.setDescription("Быстрый платеж через СБП");
        methods.add(sbp);

        return methods;
    }
}
