package org.example.kortex.products.domain;

import javax.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.products.api.ProductSearchFilter;
import org.example.kortex.products.api.dto.ProductPageResponse;
import org.example.kortex.products.api.mapper.ProductMapper;
import org.example.kortex.products.api.dto.ProductResponse;
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
    private final ProductMapper productMapper;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductMapper productMapper)
    {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    //================================Controller Methods================================================

    @Async("asyncExecutor")
    public CompletableFuture<ProductPageResponse> findProductsFilter(ProductSearchFilter filter) {
        log.info("Запрос на выдачу всех товаров с фильром: {}", filter);

        try {
            Category category = filter.category() != null ? Category.valueOf(filter.category()) : null;
            int pageSize = filter.size() != null ? filter.size() : 10;
            int pageNumber = filter.page() != null ? filter.page() : 0;
            String query = filter.query() != null ? filter.query() : "";

            Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

            long startTime = System.currentTimeMillis();
            Page<Product> productsPage = productRepository.findProductsFilter(category, query, pageable);
            long endTime = System.currentTimeMillis();

            log.info("Поиск завершен за {} мс, найдено: {} товаров",
                    (endTime - startTime), productsPage.getTotalElements());

            ProductPageResponse response = productMapper.toPageResponse(productsPage);
            return CompletableFuture.completedFuture(response);

        } catch (Exception ex) {
            log.error("Ошибка при загрузке продуктов: {}", ex.getMessage(), ex);
            return null;
        }
    }

    public ProductResponse getProductDto(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Продукт не найден"));
    }

    //================================Service Methods================================================

    public Product getById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Продукт не найден"));
    }

    public List<ProductResponse> getProductsBySeller(Long sellerId) {
        log.info("Запрос на товары у продавца: {}", sellerId);
        return productMapper.toDtoList(productRepository.findBySellerId(sellerId));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void productSubtractQuantity(Long productId, int quantity) {
        try {
            Product product = getById(productId);
            product.setCount(product.getCount() - quantity);
            productRepository.save(product);
        }catch (Exception e){
            log.error("Ошибка уменьшение количество продукта productId: {}, ex={}", productId, e.getMessage());
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void productAddQuantity(Long productId, int quantity) {
        try {
            Product product = getById(productId);
            product.setCount(product.getCount() + quantity);
            productRepository.save(product);
        }catch (Exception e){
            log.error("Ошибка добавление количество продукта productId: {}, ex={}", productId, e.getMessage());
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

