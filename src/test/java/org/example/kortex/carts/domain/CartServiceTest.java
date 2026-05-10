package org.example.kortex.carts.domain;

import org.example.kortex.carts.api.dto.CartResponse;
import org.example.kortex.carts.api.dto.CartItemResponse;
import org.example.kortex.carts.domain.mapper.CartMapper;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.db.CartRepository;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemService cartItemService;

    @Mock
    private UserService userService;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;
    private CartItem cartItem1;
    private CartItem cartItem2;
    private CartResponse cartResponse;
    private CartItemResponse cartItemResponse1;
    private CartItemResponse cartItemResponse2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("user@example.com");

        cart = new Cart();
        cart.setId(100L);
        cart.setUser(user);

        cartItem1 = new CartItem();
        cartItem1.setId(1L);
        cartItem1.setCart(cart);
        cartItem1.setPrice(new BigDecimal("99"));
        cartItem1.setQuantity(2);

        cartItem2 = new CartItem();
        cartItem2.setId(2L);
        cartItem2.setCart(cart);
        cartItem2.setPrice(new BigDecimal("149.99"));
        cartItem2.setQuantity(1);

        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem1);
        cartItems.add(cartItem2);
        cart.setCartItems(cartItems);

        user.setCart(cart);

        cartItemResponse1 = new CartItemResponse(
                1L,
                200L,
                "Product 1",
                "image1.jpg",
                new BigDecimal("99.99"),
                2
        );

        cartItemResponse2 = new CartItemResponse(
                2L,
                300L,
                "Product 2",
                "image2.jpg",
                new BigDecimal("149.99"),
                1
        );

        List<CartItemResponse> itemResponses = List.of(cartItemResponse1, cartItemResponse2);
        cartResponse = new CartResponse(
                itemResponses,
                new BigDecimal("349.97")
        );
    }


    @Test
    void getCartPage_ShouldReturnCartResponse_WhenSuccess() {
        when(userService.getCurrentUserCart()).thenReturn(user);
        when(cartMapper.toCartResponse(cart)).thenReturn(cartResponse);

        CartResponse result = cartService.getCartPage();

        assertNotNull(result);
        assertEquals(2, result.items().size());
        assertEquals(new BigDecimal("349.97"), result.total());
        assertEquals(cartResponse, result);

        verify(userService).getCurrentUserCart();
        verify(cartMapper).toCartResponse(cart);
    }

    @Test
    void addItemToCart_ShouldAddItemAndReturnUpdatedCart_WhenSuccess() {
        Long productId = 400L;
        Cart savedCart = new Cart();
        cart.setId(100L);
        cart.setUser(user);

        when(userService.getCurrentUserCart()).thenReturn(user);
        doNothing().when(cartItemService).addItemToCart(cart.getId(), productId);
        when(cartRepository.save(cart)).thenReturn(savedCart);
        when(cartMapper.toCartResponse(savedCart)).thenReturn(cartResponse);

        CartResponse result = cartService.addItemToCart(productId);

        assertNotNull(result);
        verify(userService).getCurrentUserCart();
        verify(cartItemService).addItemToCart(cart.getId(), productId);
        verify(cartRepository).save(cart);
        verify(cartMapper).toCartResponse(savedCart);
    }

    @Test
    void increaseQuantity() {
        Long itemId = 1L;
        doNothing().when(cartItemService).updateIncrement(itemId);

        cartService.increaseQuantity(itemId);

        verify(cartItemService).updateIncrement(itemId);
    }

    @Test
    void decreaseQuantity() {
        Long itemId = 1L;
        when(cartItemService.decreaseQuantityOrRemove(itemId)).thenReturn(cartItem1);

        cartService.decreaseQuantity(itemId);

        verify(cartItemService).decreaseQuantityOrRemove(itemId);
    }


    @Test
    void removeItemFromCart() {

        Long itemId = 1L;
        doNothing().when(cartItemService).removeItemFromCart(itemId);

        cartService.removeItemFromCart(itemId);

        verify(cartItemService).removeItemFromCart(itemId);
    }

    @Test
    void getCartWithUser() {
        Long userId = 1L;
        when(cartRepository.findByUserIdWithItemsAndUser(userId)).thenReturn(cart);

        Cart result = cartService.getCartWithUser(userId);

        assertNotNull(result);
        assertEquals(cart, result);
        verify(cartRepository).findByUserIdWithItemsAndUser(userId);
    }

    @Test
    void create() {
        Cart cartToCreate = new Cart();
        cartToCreate.setUser(user);

        when(cartRepository.save(cartToCreate)).thenReturn(cart);

        Cart result = cartService.create(cartToCreate);

        assertNotNull(result);
        assertEquals(cart, result);
        verify(cartRepository).save(cartToCreate);
    }

    @Test
    void clearCartByUserId() {
        Long userId = 1L;
        when(cartRepository.findByUserIdWithItemsAndUser(userId)).thenReturn(cart);
        when(cartRepository.save(cart)).thenReturn(cart);

        cartService.clearCartByUserId(userId);

        assertTrue(cart.getCartItems().isEmpty());
        verify(cartRepository).findByUserIdWithItemsAndUser(userId);
        verify(cartRepository).save(cart);
    }



}