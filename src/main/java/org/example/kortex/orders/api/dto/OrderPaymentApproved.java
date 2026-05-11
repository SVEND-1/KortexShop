package org.example.kortex.orders.api.dto;

import java.util.List;

public record OrderPaymentApproved(
        Long id,
        OrderResponseDTO orders,
        String paymentId,
        Boolean approved
) {
}
