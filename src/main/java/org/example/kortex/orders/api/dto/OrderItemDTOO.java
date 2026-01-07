package org.example.kortex.orders.api.dto;

import lombok.Data;

@Data
public class OrderItemDTOO {
    private String nameProduct;
    private Double priceProduct;
    private int count;
    private String image;
}

