package org.example.kortex.products.domain;

import javax.persistence.EntityNotFoundException;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.db.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<Product> getAvailableProductsWithPagination(int page, int size) {
        return productRepository.findByCountGreaterThan(0, PageRequest.of(page, size));
    }

    public Page<Product> getProductsByCategoryWithPagination(Product.Category category, int page, int size) {
        return productRepository.findByCategoryAndCountGreaterThan(category, 0, PageRequest.of(page, size));
    }

    public List<Product> find48Product(){
        return productRepository.findTop48ByCountGreaterThan(0);
    }

    public List<Product> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    public Product getById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Продукт не найден"));
    }

    public List<Product> getAvailableProducts(){
        return productRepository.findByCountGreaterThan(0);
    }

    public List<Product> searchProducts(String query){
        return getAvailableProducts().stream().filter(product -> product.getName().contains(query)).collect(Collectors.toList());
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
        return productRepository.save(productToCreate);
    }

    public Product update(Long id, Product productToUpdate) {
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

        return productRepository.save(existingProduct);
    }

    public void deleted(Long id) {
        if(!productRepository.existsById(id)){
            throw new NoSuchElementException("Продукт не найден");
        }
        productRepository.deleteById(id);
    }
}
