package org.example.kortex.payments.api.dto.response.payment;

public record PaymentCreateResponse(
        String paymentId,
        String urlPay,
        Long orderId
) {
}
