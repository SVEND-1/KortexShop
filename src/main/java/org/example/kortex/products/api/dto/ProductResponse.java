package org.example.kortex.products.api.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer count,
        String category,
        String image) {
}
