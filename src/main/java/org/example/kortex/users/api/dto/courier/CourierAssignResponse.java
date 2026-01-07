package org.example.kortex.users.api.dto.courier;

public record CourierAssignResponse(
        Long orderId,
        Long courierId,
        String courierName
) {
}
