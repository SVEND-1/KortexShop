package org.example.kortex.products.api.dto;

import lombok.Data;
import org.example.kortex.products.db.Product;

@Data
public class ProductDetailsDTO {
    private Long id;
    private String name;
    private Double price;
    private int count;
    private Product.Category category;
    private String description;
    private String image;
}
