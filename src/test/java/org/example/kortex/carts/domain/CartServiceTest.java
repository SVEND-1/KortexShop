package org.example.kortex.carts.domain;

import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.db.CartRepository;
import org.example.kortex.products.db.Product;
import org.example.kortex.users.db.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Optional.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemService cartItemService;

    @InjectMocks
    private CartService cartService;

    private Cart testCart;
    private CartItem cartItem1;
    private CartItem cartItem2;

    @BeforeEach
    void setUp() {
        // Создаем тестовые данные
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Тестовый товар 1");
        product1.setPrice(100.0);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Тестовый товар 2");
        product2.setPrice(200.0);

        cartItem1 = new CartItem();
        cartItem1.setId(1L);
        cartItem1.setProduct(product1);
        cartItem1.setQuantity(2);
        cartItem1.setPrice(BigDecimal.valueOf(200.0));

        cartItem2 = new CartItem();
        cartItem2.setId(2L);
        cartItem2.setProduct(product2);
        cartItem2.setQuantity(1);
        cartItem2.setPrice(BigDecimal.valueOf(200.0));

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setCartItems(Arrays.asList(cartItem1, cartItem2));
    }

    @Test
    void getCartByUserId() {
        Long userId = 1L;
        Cart cart = new Cart();
        cart.setId(1L);

        when(cartRepository.findByUserIdWithItems(userId))
                .thenReturn(cart);

        Cart result = cartService.getCartByUserId(userId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cartRepository, times(1)).findByUserIdWithItems(userId);
    }

    @Test
    void getById() {
        Long cartId = 1L;
        Cart cart = new Cart();
        cart.setId(cartId);

        when(cartRepository.findById(cartId))
                .thenReturn(Optional.of(cart));

        Cart result = cartService.getById(cartId);

        assertEquals(cartId, result.getId());
        verify(cartRepository, times(1)).findById(cartId);
    }

    @Test
    void create() {
        User user = new User();
        user.setId(1L);

        Cart cart = new Cart();
        cart.setUser(user);

        when(cartRepository.save(cart)).thenReturn(cart);

        Cart result = cartService.create(cart);

        assertNotNull(result);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void clearCartByUserId() {
        Long userId = 1L;
        Cart cart = new Cart();

        when(cartRepository.findByUserIdWithItems(userId))
                .thenReturn(cart);
        when(cartRepository.save(cart)).thenReturn(cart);

        Cart result = cartService.clearCartByUserId(userId);

        assertNotNull(result);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void cartAddProduct() {
//        Long cartId = 1L;
//        Long productId = 2L;
//        Cart cart = new Cart();
//
//        when(cartRepository.findById(cartId))
//                .thenReturn(Optional.of(cart));
//        when(cartRepository.save(cart)).thenReturn(cart);
//
//        Cart result = cartService.cartAddProduct(cartId, productId);
//
//        assertNotNull(result);
//        verify(cartItemService, times(1)).addItemToCart(cartId, productId, 1);
    }
}