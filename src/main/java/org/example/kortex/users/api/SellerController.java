package org.example.kortex.users.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kortex.products.api.dto.ProductRequest;
import org.example.kortex.users.domain.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/sellers")
@Tag(name = "Seller",description = "Работа с продавцами")
public class SellerController {
    private final SellerService sellerService;

    @Autowired
    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Operation(summary = "Получение товаров продавца")
    @GetMapping("/products")
    public ResponseEntity<?> getMyProducts() {
        return ResponseEntity.ok(sellerService.getMyProducts());
    }

    @Operation(summary = "Получение деталей товара")
    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(sellerService.getProduct(id));
    }

    @Operation(summary = "Создание товара")
    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(@ModelAttribute @Valid ProductRequest request) {
        return ResponseEntity.ok(sellerService.createProduct(request));
    }

    @Operation(summary = "Обновление товара")
    @PutMapping(value = "/products/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @ModelAttribute @Valid ProductRequest request) {
        return ResponseEntity.ok(sellerService.updateProduct(id, request));
    }

    @Operation(summary = "Удаление товара")
    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(sellerService.deleteProduct(id));
    }

}
