package org.example.kortex.carts.domain;

import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.db.CartItemRepository;
import org.example.kortex.carts.db.CartRepository;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.db.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartItemServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartItemService cartItemService;

    private Cart cart;
    private Product product;
    private CartItem existingCartItem;
    private CartItem newCartItem;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setId(1L);

        product = new Product();
        product.setId(100L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("99.99"));
        product.setCount(10);

        existingCartItem = new CartItem();
        existingCartItem.setId(1L);
        existingCartItem.setCart(cart);
        existingCartItem.setProduct(product);
        existingCartItem.setQuantity(2);
        existingCartItem.setPrice(new BigDecimal("199.98"));


        newCartItem = new CartItem();
        newCartItem.setId(2L);
        newCartItem.setCart(cart);
        newCartItem.setProduct(product);
        newCartItem.setQuantity(1);
        newCartItem.setPrice(new BigDecimal("99.99"));
    }

    @Test
    void addItemToCart() {
        Long cartId = 1L;
        Long productId = 100L;

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.of(existingCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(existingCartItem);

        cartItemService.addItemToCart(cartId, productId);

        assertEquals(3, existingCartItem.getQuantity());
        verify(cartRepository).findById(cartId);
        verify(productRepository).findById(productId);
        verify(cartItemRepository).findByCartIdAndProductId(cartId, productId);
        verify(cartItemRepository).save(existingCartItem);
    }


    @Test
    void updateIncrement() {
        Long cartItemId = 1L;
        product.setCount(10);
        existingCartItem.setQuantity(2);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(existingCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(existingCartItem);

        cartItemService.updateIncrement(cartItemId);

        assertEquals(3, existingCartItem.getQuantity());
        verify(cartItemRepository).findById(cartItemId);
        verify(cartItemRepository).save(existingCartItem);
    }


    @Test
    void decreaseQuantityOrRemove() {
        Long cartItemId = 1L;
        existingCartItem.setQuantity(3);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(existingCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(existingCartItem);

        CartItem result = cartItemService.decreaseQuantityOrRemove(cartItemId);

        assertNotNull(result);
        assertEquals(2, existingCartItem.getQuantity());
        verify(cartItemRepository).findById(cartItemId);
        verify(cartItemRepository).save(existingCartItem);
        verify(cartItemRepository, never()).delete(any());
    }


    @Test
    void removeItemFromCart_ShouldDeleteCartItem_WhenSuccess() {
        Long cartItemId = 1L;

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(existingCartItem));
        doNothing().when(cartItemRepository).delete(existingCartItem);

        cartItemService.removeItemFromCart(cartItemId);

        verify(cartItemRepository).findById(cartItemId);
        verify(cartItemRepository).delete(existingCartItem);

        verify(productRepository, never()).save(any(Product.class));

        assertEquals(10, product.getCount());
    }


}