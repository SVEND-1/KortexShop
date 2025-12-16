package org.example.kortex.products.api;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.products.api.dto.ProductMapper;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.domain.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


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
    public ResponseEntity<?> getProducts(@RequestParam(name = "category",required = false) String category,
                                         @RequestParam(name = "query", required = false) String query,
                                         @RequestParam(name = "page", defaultValue = "0") Integer page,
                                         @RequestParam(name = "size", defaultValue = "12") Integer size) {
        try {
            ProductSearchFilter filter = new ProductSearchFilter(category, query, size, page);

            Page<Product> productsPage = productService.findProductsFilter(filter);

            Map<String, Object> response = productMapper.toPageResponse(productsPage);

            return ResponseEntity.ok().body(response);
        }
        catch (Exception e) {
            log.error("Не удалось загрузить товары для пользователя " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> productDetailPage(@PathVariable String id)  {
        try {
            Product product = productService.getById(Long.parseLong(id));
            return ResponseEntity.ok(productMapper.toDtoResponse(product));
        }
        catch (Exception e) {
            log.error("Не удалось загрузить данные товара " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

