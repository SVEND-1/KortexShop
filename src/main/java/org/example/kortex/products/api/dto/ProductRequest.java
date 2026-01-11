package org.example.kortex.products.api.dto;

import org.example.kortex.products.db.Category;
import org.example.kortex.products.db.Product;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
        @NotNull
        @Size(min = 1, max = 128)
        String name,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal price,

        @NotNull
        @Min(value = 0)
        Integer count,

        @NotNull
        @Size(min = 1, max = 3000)
        String description,

        @NotNull
        Category category,

        @NotNull
        MultipartFile imageFile
) {
}
