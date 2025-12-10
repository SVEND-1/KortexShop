package org.example.kortex.products.domain;

import javax.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.products.api.ProductSearchFilter;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.db.ProductRepository;
import org.example.kortex.users.domain.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    public List<Product> findProductsFilter(ProductSearchFilter filter){
        log.info("Запрос на выдачу всех товаров с фильром: " + filter);
        Product.Category category = filter.category() != null ? Product.Category.valueOf(filter.category()) : null;
        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
        String query = filter.query() != null ? filter.query() : "";

        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        List<Product> products = productRepository.findProductsFilter(category,query,pageable);
        log.info("Выдача всех товаров с фильтром: " + filter);
        return products;
    }

    public List<Product> getProductsBySeller(Long sellerId) {
        log.info("Запрос на товары у продавца: " + sellerId);
        List<Product> products = productRepository.findBySellerId(sellerId);
        log.info("Запрос на товары у продавца успешно выполнен");
        return products;
    }

    public Product getById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Продукт не найден"));
    }

    public Product productSubtractQuantity(Long productId, int quantity) {
        Product product = getById(productId);
        product.setCount(product.getCount() - quantity);
        return productRepository.save(product);
    }

    public Product productAddQuantity(Long productId, int quantity) {
        Product product = getById(productId);
        product.setCount(product.getCount() + quantity);
        return productRepository.save(product);
    }


    public Product create(Product productToCreate) {
        log.info("Создания продкута");
        Product product = productRepository.save(productToCreate);
        log.info("Продукт создан id: " + product.getId());
        return product;
    }

    public Product update(Long id, Product productToUpdate) {
        log.info("Обновлние продукта с id: " + id);
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
        log.info("Продукт обновлени id: " + productUpdated.getId());
        return productRepository.save(productUpdated);
    }

    public void deleted(Long id) {
        if(!productRepository.existsById(id)){
            throw new NoSuchElementException("Продукт не найден");
        }
        productRepository.deleteById(id);
        log.info("Продукт удален id: " + id);
    }
}
