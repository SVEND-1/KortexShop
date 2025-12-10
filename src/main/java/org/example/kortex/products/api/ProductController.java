package org.example.kortex.products.api;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.domain.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<?> getProducts(@RequestParam(name = "category",required = false) String category,
                                         @RequestParam(name = "query", required = false) String query,
                                         @RequestParam(name = "pageSize",required = false) Integer pageSize,
                                         @RequestParam(name = "pageNumber", required = false) Integer pageNumber) {
        //Можно фильтровать по катерогиям и запросы,можнол только по категориям и только по запросу ,а можно вообще полный список
        try {
            ProductSearchFilter filter = new ProductSearchFilter(category, query, pageSize, pageNumber);
            return ResponseEntity.ok().body(productService.findProductsFilter(filter));
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
            return ResponseEntity.ok(product);
        }
        catch (Exception e) {
            log.error("Не удалось загрузить данные товара " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
