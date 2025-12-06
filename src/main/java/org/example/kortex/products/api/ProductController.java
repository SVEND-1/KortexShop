package org.example.kortex.products.api;

import org.example.kortex.products.db.Product;
import org.example.kortex.products.domain.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        ProductSearchFilter filter = new ProductSearchFilter(category, query, pageSize, pageNumber);
        return ResponseEntity.ok().body(productService.findProductsFilter(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> productDetailPage(@PathVariable String id, Model model)  {
        Product product = productService.getById(Long.parseLong(id));
        return ResponseEntity.ok(product);
    }
}
