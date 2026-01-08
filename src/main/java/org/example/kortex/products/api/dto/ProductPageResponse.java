package org.example.kortex.products.api.dto;

import java.util.List;

public record ProductPageResponse(
        List<ProductResponse> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
) {
}
