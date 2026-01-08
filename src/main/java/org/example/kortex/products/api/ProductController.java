package org.example.kortex.products.api;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.products.api.dto.ProductMapper;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.domain.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    @Autowired
    public ProductController(ProductService productService, ProductMapper productMapper)
    {
        this.productService = productService;
        this.productMapper = productMapper;
    }


    @GetMapping
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getProducts(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "12") Integer size) {

        ProductSearchFilter filter = new ProductSearchFilter(category, query, size, page);

        return productService.findProductsFilter(filter)
                .thenApply(productsPage -> {
                    Map<String, Object> response = productMapper.toPageResponse(productsPage);
                    return ResponseEntity.ok(response);
                })
                .exceptionally(ex -> {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Ошибка при загрузке товаров");
                    error.put("message", ex.getMessage());
                    log.error("Ошибка при загрузке продуктов: {}", ex.getMessage());
                    return ResponseEntity.badRequest().body(error);
                });
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> productDetailPage(@PathVariable String id)  {
        Product product = productService.getById(Long.parseLong(id));//TODO Сделать сразу DTO
        return ResponseEntity.ok(productMapper.toDtoResponse(product));
    }


}

