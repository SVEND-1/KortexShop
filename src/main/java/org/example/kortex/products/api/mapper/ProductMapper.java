package org.example.kortex.products.api.mapper;

import org.example.kortex.products.api.dto.ProductPageResponse;
import org.example.kortex.products.api.dto.ProductResponse;
import org.example.kortex.products.db.Product;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper
public interface ProductMapper {

    ProductResponse toDto(Product productResponse);


    List<ProductResponse> toDtoList(List<Product> productResponses);

    default ProductPageResponse toPageResponse(Page<Product> productResponses) {
        if (productResponses == null) {
            return null;
        }

        return new ProductPageResponse(
                toDtoList(productResponses.getContent()),
                productResponses.getNumber(),
                productResponses.getSize(),
                productResponses.getTotalElements(),
                productResponses.getTotalPages(),
                productResponses.isFirst(),
                productResponses.isLast(),
                productResponses.isEmpty()
        );
    }
}
