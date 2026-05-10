package org.example.kortex.users.domain;

import org.example.kortex.products.domain.mapper.ProductMapper;
import org.example.kortex.products.api.dto.ProductRequest;
import org.example.kortex.products.api.dto.ProductResponse;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.domain.ProductService;
import org.example.kortex.users.db.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.example.kortex.products.db.Category.ELECTRONICS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private SellerService sellerService;

    private User testUser;
    private Product testProduct;
    private ProductResponse testProductResponse;
    private MultipartFile mockImage;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("testSeller");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        testProduct.setCount(10);
        testProduct.setCategory(ELECTRONICS);
        testProduct.setSeller(testUser);

        testProductResponse = new ProductResponse(
                1L,
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                10,
                "ELECTRONICS",
                "image.jpg"
        );

        mockImage = mock(MultipartFile.class);
    }

    @Test
    void getMyProducts() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(productService.getProductsBySeller(1L)).thenReturn(Arrays.asList(testProductResponse));

        List<ProductResponse> result = sellerService.getMyProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProductResponse, result.get(0));
        verify(userService).getCurrentUser();
        verify(productService).getProductsBySeller(1L);
    }

    @Test
    void getProduct() {
        when(productService.getById(1L)).thenReturn(testProduct);
        when(productMapper.toDto(testProduct)).thenReturn(testProductResponse);

        ProductResponse result = sellerService.getProduct(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Test Product", result.name());
        verify(productService).getById(1L);
        verify(productMapper).toDto(testProduct);
    }

    @Test
    void createProduct() throws IOException {
        ProductRequest testProductRequest = new ProductRequest(
                "New Product",
                BigDecimal.valueOf(49.99),
                5,
                "New Description",
                ELECTRONICS,
                mockImage
        );

        when(mockImage.isEmpty()).thenReturn(false);
        when(mockImage.getOriginalFilename()).thenReturn("test.jpg");
        when(mockImage.getInputStream()).thenReturn(mock(InputStream.class));

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(productService.create(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toDto(testProduct)).thenReturn(testProductResponse);

        ProductResponse result = sellerService.createProduct(testProductRequest);

        assertNotNull(result);
        verify(userService).getCurrentUser();
        verify(productService).create(any(Product.class));
        verify(productMapper).toDto(testProduct);
    }


    @Test
    void updateProduct() throws IOException {
        ProductRequest testProductRequest = new ProductRequest(
                "Updated Product",
                BigDecimal.valueOf(79.99),
                15,
                "Updated Description",
                ELECTRONICS,
                mockImage
        );

        when(mockImage.isEmpty()).thenReturn(false);
        when(mockImage.getOriginalFilename()).thenReturn("updated.jpg");
        when(mockImage.getInputStream()).thenReturn(mock(InputStream.class));

        when(productService.getById(1L)).thenReturn(testProduct);
        when(productService.update(eq(1L), any(Product.class))).thenReturn(testProduct);
        when(productMapper.toDto(testProduct)).thenReturn(testProductResponse);

        ProductResponse result = sellerService.updateProduct(1L, testProductRequest);

        assertNotNull(result);
        verify(productService).getById(1L);
        verify(productService).update(eq(1L), any(Product.class));
        verify(productMapper).toDto(testProduct);
    }


    @Test
    void deleteProduct() {
        when(productService.getById(1L)).thenReturn(testProduct);
        doNothing().when(productService).deleted(1L);

        boolean result = sellerService.deleteProduct(1L);

        assertTrue(result);
        verify(productService).getById(1L);
        verify(productService).deleted(1L);
    }

}