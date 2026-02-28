package org.example.kortex.products.domain;

import org.example.kortex.products.api.ProductSearchFilter;
import org.example.kortex.products.api.dto.ProductPageResponse;
import org.example.kortex.products.api.dto.ProductResponse;
import org.example.kortex.products.api.mapper.ProductMapper;
import org.example.kortex.products.db.Category;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.db.ProductRepository;
import org.example.kortex.users.db.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private User seller;
    private Product product;
    private ProductResponse productResponse;
    private ProductSearchFilter filter;

//    @BeforeEach
//    void setUp() {
//        seller = new User();
//        seller.setId(1L);
//        seller.setName("Продавец");
//        seller.setEmail("seller@example.com");
//
//        product = new Product();
//        product.setId(100L);
//        product.setSeller(seller);
//        product.setName("Test Product");
//        product.setPrice(new BigDecimal("99.99"));
//        product.setCount(10);
//        product.setDescription("Test Description");
//        product.setImage("image.jpg");
//        product.setCategory(Category.ELECTRONICS);
//
//        productResponse = new ProductResponse(
//                100L,
//                "Test Product",
//                "Test Description",
//                new BigDecimal("99.99"),
//                10,
//                Category.ELECTRONICS.name(),
//                "image.jpg"
//        );
//
//
//        filter = new ProductSearchFilter(
//                "ELECTRONICS",
//                "test",
//                10,
//                0
//        );
//    }
//
//
//    @Test
//    void findProductsFilter() throws ExecutionException, InterruptedException {
//        // Arrange
//        Page<Product> productPage = new PageImpl<>(
//                List.of(product),
//                Pageable.ofSize(10).withPage(0),
//                1
//        );
//
//        ProductPageResponse pageResponse = new ProductPageResponse(
//                List.of(productResponse),
//                0,
//                10,
//                1,
//                1,
//                false,
//                false,
//                false
//        );
//
//        when(productRepository.findProductsFilter(
//                eq(Category.ELECTRONICS),
//                eq("test"),
//                any(Pageable.class)
//        )).thenReturn(productPage);
//        when(productMapper.toPageResponse(productPage)).thenReturn(pageResponse);
//
//        CompletableFuture<ProductPageResponse> future = productService.findProductsFilter(filter);
//        ProductPageResponse result = future.get();
//
//        assertNotNull(result);
//        assertEquals(1, result.totalPages());
//        assertEquals(1, result.totalElements());
//        assertEquals(1, result.content().size());
//        assertEquals(productResponse, result.content().get(0));
//
//        verify(productRepository).findProductsFilter(
//                eq(Category.ELECTRONICS),
//                eq("test"),
//                any(Pageable.class)
//        );
//        verify(productMapper).toPageResponse(productPage);
//    }
//
//    @Test
//    void getProductDto() {
//        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
//        when(productMapper.toDto(product)).thenReturn(productResponse);
//
//        ProductResponse result = productService.getProductDto(100L);
//
//        assertNotNull(result);
//        assertEquals(productResponse, result);
//        verify(productRepository).findById(100L);
//        verify(productMapper).toDto(product);
//    }
//
//
//    @Test
//    void getById() {
//        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
//
//        Product result = productService.getById(100L);
//
//        assertNotNull(result);
//        assertEquals(product, result);
//        verify(productRepository).findById(100L);
//    }
//
//
//
//    @Test
//    void getProductsBySeller() {
//        List<Product> products = List.of(product);
//        List<ProductResponse> responses = List.of(productResponse);
//
//        when(productRepository.findBySellerId(1L)).thenReturn(products);
//        when(productMapper.toDtoList(products)).thenReturn(responses);
//
//        List<ProductResponse> result = productService.getProductsBySeller(1L);
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals(productResponse, result.get(0));
//        verify(productRepository).findBySellerId(1L);
//        verify(productMapper).toDtoList(products);
//    }
//
//
//    @Test
//    void productSubtractQuantity() {
//        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
//        when(productRepository.save(any(Product.class))).thenReturn(product);
//
//        productService.productSubtractQuantity(100L, 5);
//
//        assertEquals(5, product.getCount());
//        verify(productRepository).findById(100L);
//        verify(productRepository).save(product);
//    }
//
//    @Test
//    void productAddQuantity() {
//        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
//        when(productRepository.save(any(Product.class))).thenReturn(product);
//
//        productService.productAddQuantity(100L, 5);
//
//        assertEquals(15, product.getCount());
//        verify(productRepository).findById(100L);
//        verify(productRepository).save(product);
//    }
//
//
//    @Test
//    void create() {
//        when(productRepository.save(product)).thenReturn(product);
//
//        Product result = productService.create(product);
//
//        assertNotNull(result);
//        assertEquals(product, result);
//        verify(productRepository).save(product);
//    }
//
//    @Test
//    void update() {
//        Product updatedProduct = new Product();
//        updatedProduct.setName("обновленный продукт");
//        updatedProduct.setPrice(new BigDecimal("149.99"));
//        updatedProduct.setCount(20);
//        updatedProduct.setDescription("Updated Description");
//        updatedProduct.setCategory(Category.CLOTHING);
//        updatedProduct.setImage("new_image.jpg");
//
//
//        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
//        when(productRepository.save(any(Product.class))).thenReturn(product);
//
//        Product result = productService.update(100L, updatedProduct);
//
//        assertNotNull(result);
//        assertEquals("обновленный продукт", product.getName());
//        assertEquals(new BigDecimal("149.99"), product.getPrice());
//        assertEquals(20, product.getCount());
//        assertEquals("Updated Description", product.getDescription());
//        assertEquals(Category.CLOTHING, product.getCategory());
//        assertEquals("new_image.jpg", product.getImage());
//
//        verify(productRepository).findById(100L);
//        verify(productRepository, times(2)).save(any(Product.class));
//    }
//
//    @Test
//    void deleted() {
//        when(productRepository.existsById(100L)).thenReturn(true);
//
//        productService.deleted(100L);
//
//        verify(productRepository).existsById(100L);
//        verify(productRepository).deleteById(100L);
//    }

}