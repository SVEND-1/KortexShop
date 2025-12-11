package org.example.kortex.carts.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productDescription;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private String productImage;
    private String productCategory;
}
