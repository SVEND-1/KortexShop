package org.example.kortex.products.domain;

import javax.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.products.api.dto.ProductSearchFilter;
import org.example.kortex.products.api.dto.ProductPageResponse;
import org.example.kortex.products.domain.mapper.ProductMapper;
import org.example.kortex.products.api.dto.ProductResponse;
import org.example.kortex.products.db.Category;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.db.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final RedisTemplate<String, Product> redisTemplate;
    private static final String CACHE_KEY_PREFIX = "product:";
    private static final long CACHE_TTL_MINUTES = 1;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductMapper productMapper, RedisTemplate<String, Product> redisTemplate)
    {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.redisTemplate = redisTemplate;
    }

    //================================Controller Methods================================================

    @Async("asyncExecutor")
    public CompletableFuture<ProductPageResponse> findProductsFilter(ProductSearchFilter filter) {
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
        return productMapper.toDto(getById(id));
    }

    //================================Service Methods================================================

    public Product getByIdEntity(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Продукт не найден"));
    }

    public Product getById(Long id) {
        try {
            Product product = redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + id);
            if (product != null) {
                log.debug("Продукт найден в кэше key={}", CACHE_KEY_PREFIX + id);
                return product;
            }

            product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Продукт не найден"));
            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + id, product, CACHE_TTL_MINUTES, TimeUnit.MINUTES);

            return product;
        }
        catch (Exception e){
            log.error("REDIS ex={}",e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<ProductResponse> getProductsBySeller(Long sellerId) {
        return productMapper.toDtoList(productRepository.findBySellerId(sellerId));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void productSubtractQuantity(Long productId, int quantity) {
        try {
            Product product = getByIdEntity(productId);
            product.setCount(product.getCount() - quantity);
            productRepository.save(product);

            String cacheKey = CACHE_KEY_PREFIX + productId;
            redisTemplate.delete(cacheKey);
        }catch (Exception e){
            log.error("Ошибка уменьшение количество продукта productId: {}, ex={}", productId, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void productAddQuantity(Long productId, int quantity) {
        try {
            Product product = getByIdEntity(productId);
            product.setCount(product.getCount() + quantity);
            productRepository.save(product);

            String cacheKey = CACHE_KEY_PREFIX + productId;
            redisTemplate.delete(cacheKey);
        }catch (Exception e){
            log.error("Ошибка добавление количество продукта productId: {}, ex={}", productId, e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public Product create(Product productToCreate) {
        try {
            return productRepository.save(productToCreate);
        }catch (Exception e){
            log.error("Ошибка сохранение продукта");
            throw new RuntimeException(e);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Product update(Long id, Product productToUpdate) {
        try {
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

            String cacheKey = CACHE_KEY_PREFIX + id;
            redisTemplate.delete(cacheKey);

            return productUpdated;
        }
        catch (Exception e){
            log.error("Ошибка обновление продукта с id: {}, ex={}", id, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleted(Long id) {
        try {
            if (!productRepository.existsById(id)) {
                log.info("Продукт не найден id={}",id);
                throw new NoSuchElementException("Продукт не найден");
            }
            productRepository.deleteById(id);

            String cacheKey = CACHE_KEY_PREFIX + id;
            redisTemplate.delete(cacheKey);
        }
        catch (Exception e){
            log.error("Ошибка удаление продукта с id: {}, ex={}", id, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

