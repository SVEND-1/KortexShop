package org.example.kortex.controller;

import org.example.kortex.entity.Product;
import org.example.kortex.service.ProductService;
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
public class ProductRestController {
    private final ProductService productService;

    @Autowired
    public ProductRestController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<?> getProducts(@RequestParam(required = false) String category,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "12") int size) {
        Page<Product> productsPage;
        List<Product> availableProducts;

        if (category != null && !category.isEmpty()) {
            try {
                Product.Category productCategory = Product.Category.valueOf(category.toUpperCase());
                productsPage = productService.getProductsByCategoryWithPagination(productCategory, page, size);
            } catch (IllegalArgumentException e) {
                productsPage = productService.getAvailableProductsWithPagination(page, size);
            }
        } else {
            productsPage = productService.getAvailableProductsWithPagination(page, size);
        }

        availableProducts = productsPage.getContent().stream()
                .filter(product -> product.getCount() > 0)
                .collect(Collectors.toList());


        Map<String, Object> response = new HashMap<>();
        response.put("redirectUrl", "/");
        response.put("products", availableProducts);
        response.put("categories", Product.Category.values());
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("totalPages", productsPage.getTotalPages());
        response.put("totalItems", productsPage.getTotalElements());
        response.put("hasNext", productsPage.hasNext());
        response.put("hasPrevious", productsPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> productDetailPage(@PathVariable String id, Model model)  {
        Product product = productService.getById(Long.parseLong(id));
        return ResponseEntity.ok(product);
    }
}
