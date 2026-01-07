package org.example.kortex.products.api.dto;

import org.example.kortex.products.db.Category;
import org.example.kortex.products.db.Product;
import org.example.kortex.users.api.dto.user.UserDTO;

public record ProductDTO(
        Long id,
        String name,
        Double price,
        Integer count,
        String description,
        String image,
        Category category,
        Long sellerId
) {
}
