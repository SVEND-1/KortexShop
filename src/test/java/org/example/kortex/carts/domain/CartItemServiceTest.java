package org.example.kortex.carts.domain;

import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.db.CartItemRepository;
import org.example.kortex.carts.db.CartRepository;
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

    @Test
    void findById() {
        Long itemId = 1L;
        CartItem cartItem = new CartItem();

        when(cartItemRepository.findById(itemId))
                .thenReturn(Optional.of(cartItem));

        CartItem result = cartItemService.findById(itemId);

        assertNotNull(result);
    }

    @Test
    void findAllByCartId() {
        Long cartId = 1L;
        when(cartItemRepository.findAllByCartId(cartId))
                .thenReturn(List.of(new CartItem()));

        List<CartItem> result = cartItemService.findAllByCartId(cartId);

        assertEquals(1, result.size());
    }

    @Test
    void addItemToCart() {
        Long cartId = 1L;
        Long productId = 1L;
        Cart cart = new Cart();
        Product product = new Product();

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId))
                .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(new CartItem());

        CartItem result = cartItemService.addItemToCart(cartId, productId, 1);

        assertNotNull(result);
    }

//    @Test
//    void updateQuantity() {
//        Long itemId = 1L;
//        CartItem cartItem = new CartItem();
//        cartItem.setQuantity(1);
//
//        when(cartItemRepository.findById(itemId))
//                .thenReturn(Optional.of(cartItem));
//        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
//
//        CartItem result = cartItemService.(itemId, 5);
//
//        assertEquals(5, result.getQuantity());
//    }

    @Test
    void removeItemFromCart() {
        Long itemId = 1L;
        when(cartItemRepository.existsById(itemId)).thenReturn(true);

        cartItemService.removeItemFromCart(itemId);

        verify(cartItemRepository, times(1)).deleteById(itemId);
    }
}