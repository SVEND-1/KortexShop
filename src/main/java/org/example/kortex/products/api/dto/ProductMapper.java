package org.example.kortex.products.api.dto;

import org.example.kortex.products.db.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public ProductResponse toDtoResponse(Product product) {
        if (product == null) {
            return null;
        }

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCount(),
                product.getCategory() != null ? product.getCategory().name() : null,
                product.getImage()
        );
    }

    public List<ProductResponse> toDtoListResponse(List<Product> products) {
        return products.stream()
                .map(this::toDtoResponse)
                .collect(Collectors.toList());
    }

    public ProductPageResponse toPageResponse(Page<Product> productPage) {

        return new ProductPageResponse(
                toDtoListResponse(productPage.getContent()),
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isFirst(),
                productPage.isLast(),
                productPage.isEmpty()

        );
    }
}

