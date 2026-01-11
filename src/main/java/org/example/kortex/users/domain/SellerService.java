package org.example.kortex.users.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.products.api.mapper.ProductMapper;
import org.example.kortex.products.api.dto.ProductRequest;
import org.example.kortex.products.api.dto.ProductResponse;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.domain.ProductService;
import org.example.kortex.users.db.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
@Service
public class SellerService {
    private final ProductService productService;
    private final UserService userService;
    private final ProductMapper productMapper;

    @Autowired
    public SellerService(ProductService productService, UserService userService, ProductMapper productMapper) {
        this.productService = productService;
        this.userService = userService;
        this.productMapper = productMapper;
    }

    //================================Controller Methods================================================

    public List<ProductResponse> getMyProducts() {
        try {
            User seller = userService.getCurrentUser();
            List<ProductResponse> products = productService.getProductsBySeller(seller.getId());
            log.info("Выданы продукты продовца");
            return products;
        } catch (Exception e) {
            log.error("Ошибка при получении товаров, ex={}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public ProductResponse getProduct(Long id) {
        try {
            Product product = productService.getById(id);
            return productMapper.toDto(product);
        } catch (Exception e) {
            log.error("Ошибка поиска продукта, ex={}", e.getMessage());
            return null;
        }
    }

    public ProductResponse createProduct(ProductRequest request) {
        try {
            log.info("Создания товара");
            User seller = userService.getCurrentUser();
            Product product = new Product();

            product.setName(request.name());
            product.setPrice(request.price());
            product.setCount(request.count());
            product.setDescription(request.description());
            product.setCategory(request.category());
            product.setSeller(seller);

            if (request.imageFile() != null && !request.imageFile().isEmpty()) {
                String imageName = saveImage(request.imageFile());
                product.setImage(imageName);
                log.debug("Картинка продукта сохранена");
            }

            Product createdProduct = productService.create(product);

            log.info("Товар создан успешно id={}", createdProduct.getId());

            return productMapper.toDto(createdProduct);
        } catch (Exception e) {
            log.error("Ошибка при создании товара, ex={} ", e.getMessage());
            return null;
        }
    }


    public ProductResponse updateProduct(Long id, ProductRequest request) {

        try {
            log.info("Обновление товара с id={}", id);
            Product existingProduct = productService.getById(id);

            existingProduct.setName(request.name());
            existingProduct.setPrice(request.price());
            existingProduct.setCount(request.count());
            existingProduct.setDescription(request.description());
            existingProduct.setCategory(request.category());

            if (request.imageFile() != null && !request.imageFile().isEmpty()) {
                if (existingProduct.getImage() != null) {
                    deleteImage(existingProduct.getImage());
                    log.debug("Старая картинка удалена");
                }

                String imageName = saveImage(request.imageFile());
                existingProduct.setImage(imageName);
            }

            Product updatedProduct = productService.update(id, existingProduct);

            log.info("Товар успешно обновлен");
            return productMapper.toDto(updatedProduct);
        } catch (Exception e) {
            log.error("Ошибка при обновлении товара, ex={}", e.getMessage());
            return null;
        }
    }


    public boolean deleteProduct(Long id) {//TODO Поменять Return
        try {
            log.info("Удаление товара с id, ex={}", id);
            Product product = productService.getById(id);

            if (product.getImage() != null && !product.getImage().isEmpty()) {
                deleteImage(product.getImage());
            }

            productService.deleted(id);
            log.info("Товар успешно удален");
            return true;
        } catch (Exception e) {
            log.error("Ошибка при удалении товара, ex={}",e.getMessage());
            return false;
        }
    }

    //================================Service Methods================================================


    private String saveImage(MultipartFile imageFile)  {
        try {
            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path uploadPath = Paths.get("uploads/images");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);

            try (InputStream inputStream = imageFile.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            return fileName;
        }
        catch (IOException e) {
            log.error("Не удалось сохранить картинку товара");
            return null;
        }
    }

    private void deleteImage(String imageName) {
        try {
            Path uploadPath = Paths.get("uploads/images");
            Path filePath = uploadPath.resolve(imageName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Не удалось удалить изображение image={} ", imageName);
        }
    }
}
