package org.example.kortex.products.api.dto;

import org.example.kortex.products.db.Product;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public record ProductRequest(
        String name,
        Double price,
        Integer count,
        String description,
        Product.Category category,
        MultipartFile imageFile
) {

}
