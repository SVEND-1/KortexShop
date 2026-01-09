package org.example.kortex.products.api.dto;

public record ProductResponse(
        Long id,
        String name,
        String description,
        Double price,
        Integer count,
        String category,
        String image) {
}
