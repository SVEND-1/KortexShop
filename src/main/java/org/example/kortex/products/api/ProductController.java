package org.example.kortex.products.api;

import org.example.kortex.products.api.dto.ProductPageResponse;
import org.example.kortex.products.domain.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService)
    {
        this.productService = productService;
    }


    @GetMapping
    public CompletableFuture<ResponseEntity<ProductPageResponse>> getProducts(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "12") Integer size) {

        ProductSearchFilter filter = new ProductSearchFilter(category, query, size, page);
        return productService.findProductsFilter(filter)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> productDetailPage(@PathVariable String id)  {
        return ResponseEntity.ok(productService.getProductDto(Long.parseLong(id)));
    }


}

