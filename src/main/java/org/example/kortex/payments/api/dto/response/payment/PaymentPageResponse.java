package org.example.kortex.payments.api.dto.response.payment;

import java.util.List;

public record PaymentPageResponse(
        List<PaymentResponse> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
) {
}
