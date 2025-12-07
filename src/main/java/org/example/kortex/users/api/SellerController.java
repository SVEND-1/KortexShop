package org.example.kortex.users.api;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.domain.ProductService;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/sellers")
public class SellerController {
    private final ProductService productService;
    private final UserService userService;

    @Autowired
    public SellerController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping("/products")
    public ResponseEntity<?> getMyProducts() {
        try {
            User seller = userService.getCurrentUser();
            List<Product> products = productService.getProductsBySeller(seller.getId());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при получении товаров: " + e.getMessage());
            log.error("Ошибка при получении товаров: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        try {
            User seller = userService.getCurrentUser();
            Product product = productService.getById(id);

            if (product == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Товар не найден");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            if (!product.getSeller().getId().equals(seller.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Этот товар не принадлежит вам");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            return ResponseEntity.ok(product);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка: " + e.getMessage());
            log.error("Ошибка: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }


    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam("count") Integer count,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("category") Product.Category category,
            @RequestParam(value = "imageFile") MultipartFile imageFile) {

        try {
            log.info("Создания товара ");
            User seller = userService.getCurrentUser();
            Product product = new Product();

            product.setName(name);
            product.setPrice(price);
            product.setCount(count);
            product.setDescription(description);
            product.setCategory(category);
            product.setSeller(seller);

            if (imageFile != null && !imageFile.isEmpty()) {
                String imageName = saveImage(imageFile);
                product.setImage(imageName);
            }

            Product createdProduct = productService.create(product);


            Map<String, Object> response = new HashMap<>();
            response.put("message", "Товар с изображением успешно создан");
            response.put("product", createdProduct);
            log.info("Товар создан успешно id:" + createdProduct.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при создании товара: " + e.getMessage());
            log.error("Ошибка при создании товара: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }


    @PutMapping(value = "/products/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam("count") Integer count,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("category") Product.Category category,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        try {
            log.info("Обновление товара с id: " + id);
            User seller = userService.getCurrentUser();
            Product existingProduct = productService.getById(id);

            if (existingProduct == null) {//TODO в service мб
                Map<String, String> error = new HashMap<>();
                error.put("error", "Товар не найден");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            if (!existingProduct.getSeller().getId().equals(seller.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Этот товар не принадлежит вам");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            existingProduct.setName(name);
            existingProduct.setPrice(price);
            existingProduct.setCount(count);
            existingProduct.setDescription(description);
            existingProduct.setCategory(category);

            if (imageFile != null && !imageFile.isEmpty()) {
                if (existingProduct.getImage() != null) {
                    deleteImage(existingProduct.getImage());
                }

                String imageName = saveImage(imageFile);
                existingProduct.setImage(imageName);
            }

            Product updatedProduct = productService.update(id, existingProduct);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Товар с изображением успешно обновлен");
            response.put("product", updatedProduct);
            log.info("Товар успешно обновлен");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при обновлении товара: " + e.getMessage());
            log.error("Ошибка при обновлении товара: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }


    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            log.info("Удаление товара с id: " + id);
            User seller = userService.getCurrentUser();
            Product product = productService.getById(id);

            if (product == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Товар не найден");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Проверяем, принадлежит ли товар текущему продавцу
            if (!product.getSeller().getId().equals(seller.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Этот товар не принадлежит вам");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Удаляем изображение если оно есть
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                deleteImage(product.getImage());
            }

            productService.deleted(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Товар успешно удален");
            log.info("Товар успешно удален");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при удалении товара: " + e.getMessage());
            log.error("Ошибка при удалении товара: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }


    private String saveImage(MultipartFile imageFile) throws IOException {
        String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        Path uploadPath = Paths.get("uploads/images");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    private void deleteImage(String imageName) {
        try {
            Path uploadPath = Paths.get("uploads/images");
            Path filePath = uploadPath.resolve(imageName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Не удалось удалить изображение: " + imageName);
        }
    }
}