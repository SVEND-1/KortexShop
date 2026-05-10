package org.example.kortex.products.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kortex.products.api.dto.ProductPageResponse;
import org.example.kortex.products.api.dto.ProductSearchFilter;
import org.example.kortex.products.domain.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product",description = "Работа с товарами")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService)
    {
        this.productService = productService;
    }

    @Operation(summary = "Получение списка продуктов с фильтром")
    @GetMapping
    public CompletableFuture<ResponseEntity<ProductPageResponse>> getProducts(@ModelAttribute ProductSearchFilter filter) {
        return productService.findProductsFilter(filter)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Получение деталей товара")
    @GetMapping("/{id}")
    public ResponseEntity<?> productDetailPage(@PathVariable String id)  {
        return ResponseEntity.ok(productService.getProductDto(Long.parseLong(id)));
    }
}