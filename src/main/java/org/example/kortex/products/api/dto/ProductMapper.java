package org.example.kortex.products.api.dto;

import org.example.kortex.products.db.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public ProductResponse toDtoResponse(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponse dto = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCount(),
                product.getCategory() != null ? product.getCategory().name() : null,
                product.getImage()
        );

        return dto;
    }

    public List<ProductResponse> toDtoListResponse(List<Product> products) {
        return products.stream()
                .map(this::toDtoResponse)
                .collect(Collectors.toList());
    }

    public Map<String, Object> toPageResponse(Page<Product> productPage) {
        Map<String, Object> response = new HashMap<>();

        response.put("content", toDtoListResponse(productPage.getContent()));

        response.put("page", productPage.getNumber());
        response.put("size", productPage.getSize());
        response.put("totalElements", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());
        response.put("first", productPage.isFirst());
        response.put("last", productPage.isLast());
        response.put("empty", productPage.isEmpty());

        return response;
    }
}
