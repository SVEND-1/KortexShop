package org.example.kortex.users.api.dto.courier;

public record SetStatusOrderResponse(
        Long orderId,
        String status
) {
}
