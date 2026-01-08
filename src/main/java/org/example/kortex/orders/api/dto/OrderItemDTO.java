package org.example.kortex.orders.api.dto;


public record OrderItemDTO(
    String nameProduct,
    Double priceProduct,
    int count,
    String image
    ){
}

