package org.example.kortex.orders.api.dto;

import lombok.Data;

@Data
public class PaymentMethodDTO {
    private String id;
    private String name;
    private String description;
}
