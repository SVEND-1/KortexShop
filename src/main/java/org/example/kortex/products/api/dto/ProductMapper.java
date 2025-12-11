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

    public Page<ProductResponseDTO> toDtoPage(Page<Product> productPage) {
        return productPage.map(this::toDto);
    }

    // Или возвращайте Map с полной информацией о пагинации
    public Map<String, Object> toPageResponse(Page<Product> productPage) {
        Map<String, Object> response = new HashMap<>();

        // Контент
        response.put("content", toDtoList(productPage.getContent()));

        // Информация о пагинации
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
