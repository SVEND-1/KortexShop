package org.example.kortex.payments.api.dto.response.payment;

public record PaymentResponse(
        String id,
        String value,
        String description,
        String status,
        String createdAt
) {
}
