package org.example.kortex.products.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer count;
    private String category;
    private String image;
    private Long sellerId;
    private String sellerName;
    private String sellerEmail;


}
