package org.example.kortex.products.api.dto;

import org.example.kortex.products.db.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {
    public ProductResponseDTO toDto(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCount(product.getCount());
        dto.setCategory(product.getCategory() != null ? product.getCategory().name() : null);
        dto.setImage(product.getImage());

        if (product.getSeller() != null) {
            dto.setSellerId(product.getSeller().getId());
            dto.setSellerName(product.getSeller().getName());
            dto.setSellerEmail(product.getSeller().getEmail());
        }

        return dto;
    }

    public List<ProductResponseDTO> toDtoList(List<Product> products) {
        return products.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
