package org.example.kortex.products.domain;

import org.example.kortex.products.db.Product;
import org.example.kortex.products.db.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ProductService productService;

    @Test
    void findProductsFilter() {
        when(productRepository.findProductsFilter(any(), anyString(), any()))
                .thenReturn(List.of(new Product()));

        List<Product> result = productService.findProductsFilter(
                new org.example.kortex.products.api.ProductSearchFilter("ELECTRONICS", "test", 10, 0)
        );

        assertEquals(1, result.size());
    }

    @Test
    void getProductsBySeller() {
        Long sellerId = 1L;
        when(productRepository.findBySellerId(sellerId))
                .thenReturn(List.of(new Product()));

        List<Product> result = productService.getProductsBySeller(sellerId);

        assertEquals(1, result.size());
    }

    @Test
    void getById() {
        Long productId = 1L;
        Product product = new Product();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        Product result = productService.getById(productId);

        assertNotNull(result);
    }

    @Test
    void productSubtractQuantity() {
        Long productId = 1L;
        Product product = new Product();
        product.setCount(10);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.productSubtractQuantity(productId, 2);

        assertEquals(8, result.getCount());
    }

    @Test
    void productAddQuantity() {
        Long productId = 1L;
        Product product = new Product();
        product.setCount(10);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.productAddQuantity(productId, 5);

        assertEquals(15, result.getCount());
    }

    @Test
    void create() {
        Product product = new Product();

        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.create(product);

        assertNotNull(result);
    }

    @Test
    void update() {
        Long productId = 1L;
        Product product = new Product();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.update(productId, product);

        assertNotNull(result);
    }

    @Test
    void deleted() {
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(true);

        productService.deleted(productId);

        verify(productRepository, times(1)).deleteById(productId);
    }
}