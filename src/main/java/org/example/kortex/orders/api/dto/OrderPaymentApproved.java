package org.example.kortex.orders.api.dto;

import javax.validation.constraints.NotNull;

public record OrderPaymentApproved(
        @NotNull
        String paymentId,
        @NotNull
        Long orderId
) {
}
