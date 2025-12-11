package org.example.kortex.orders.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderCreatePageDTO {
    private UserInfoDTO userInfo;
    private List<CartItemOrderDTO> cartItems;
    private OrderSummaryDTO summary;
    private List<PaymentMethodDTO> paymentMethods;
}
