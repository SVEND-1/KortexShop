package org.example.kortex.users.api;

import org.example.kortex.products.api.dto.ProductRequest;
import org.example.kortex.users.domain.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {
    private final SellerService sellerService;

    @Autowired
    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping("/products")
    public ResponseEntity<?> getMyProducts() {
        return ResponseEntity.ok(sellerService.getMyProducts());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(sellerService.getProduct(id));
    }


    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(@ModelAttribute ProductRequest request) {
        return ResponseEntity.ok(sellerService.createProduct(request));
    }


    @PutMapping(value = "/products/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @ModelAttribute ProductRequest request) {
        return ResponseEntity.ok(sellerService.updateProduct(id, request));
    }


    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(sellerService.deleteProduct(id));
    }

}
