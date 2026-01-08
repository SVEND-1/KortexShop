package org.example.kortex.products.domain;

import javax.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.products.api.ProductSearchFilter;
import org.example.kortex.products.db.Category;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.db.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class ProductService {
    private final ProductRepository productRepository;
    @Autowired
    public ProductService(ProductRepository productRepository)
    {
        this.productRepository = productRepository;
    }

    @Async("asyncExecutor")
    public CompletableFuture<Page<Product>> findProductsFilter(ProductSearchFilter filter){
        log.info("Запрос на выдачу всех товаров с фильром: {}", filter);

        Category category = filter.category() != null ? Category.valueOf(filter.category()) : null;
        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
        String query = filter.query() != null ? filter.query() : "";

        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        long startTime = System.currentTimeMillis();
        Page<Product> productsPage = productRepository.findProductsFilter(category, query, pageable);
        long endTime = System.currentTimeMillis();

        log.info("Поиск завершен за {} мс, найдено: {} товаров",
                (endTime - startTime), productsPage.getTotalElements());
        return CompletableFuture.completedFuture(productsPage);
    }

    public List<Product> getProductsBySeller(Long sellerId) {
        log.info("Запрос на товары у продавца: {}", sellerId);
        List<Product> products = productRepository.findBySellerId(sellerId);
        log.info("Запрос на товары у продавца успешно выполнен");
        return products;
    }

    public Product getById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Продукт не найден"));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Product productSubtractQuantity(Long productId, int quantity) {
        try {
            Product product = getById(productId);
            product.setCount(product.getCount() - quantity);
            return productRepository.save(product);
        }catch (Exception e){
            log.error("Ошибка уменьшение количество продукта productId: {}, ex={}", productId, e.getMessage());
            return null;
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Product productAddQuantity(Long productId, int quantity) {
        try {
            Product product = getById(productId);
            product.setCount(product.getCount() + quantity);
            return productRepository.save(product);
        }catch (Exception e){
            log.error("Ошибка добавление количество продукта productId: {}, ex={}", productId, e.getMessage());
            return null;
        }
    }


    public Product create(Product productToCreate) {
        try {
            log.info("Создания продкута");
            Product product = productRepository.save(productToCreate);
            log.info("Продукт создан id: {}", product.getId());
            return product;
        }catch (Exception e){
            log.error("Ошибка сохранение продукта");
            return null;
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Product update(Long id, Product productToUpdate) {
        try {
            log.info("Обновлние продукта с id: {}", id);
            Product existingProduct = productRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Продукт не найден"));

            existingProduct.setName(productToUpdate.getName());
            existingProduct.setPrice(productToUpdate.getPrice());
            existingProduct.setCount(productToUpdate.getCount());
            existingProduct.setDescription(productToUpdate.getDescription());
            existingProduct.setCategory(productToUpdate.getCategory());

            if (productToUpdate.getImage() != null && !productToUpdate.getImage().isEmpty()) {
                existingProduct.setImage(productToUpdate.getImage());
            }

            Product productUpdated = productRepository.save(existingProduct);
            log.info("Продукт обновлени id: {}", productUpdated.getId());
            return productRepository.save(productUpdated);
        }
        catch (Exception e){
            log.error("Ошибка обновление продукта с id: {}, ex={}", id, e.getMessage());
            return null;
        }
    }

    public void deleted(Long id) {
        try {
            if (!productRepository.existsById(id)) {
                log.warn("Продукт не найден id={}",id);
                throw new NoSuchElementException("Продукт не найден");
            }
            productRepository.deleteById(id);
            log.info("Продукт удален id: {}", id);
        }
        catch (Exception e){
            log.error("Ошибка удаление продукта с id: {}, ex={}", id, e.getMessage());
        }
    }
}

