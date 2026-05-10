package org.example.kortex.orders.domain;

import org.example.kortex.carts.api.dto.CartItemResponse;
import org.example.kortex.carts.domain.mapper.CartMapper;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.orders.api.dto.CreateOrderPageDTO;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderRepository;
import org.example.kortex.users.api.dto.user.UserResponse;
import org.example.kortex.users.domain.mapper.UserMapper;
import org.example.kortex.users.db.Role;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @Mock
    private OrderCourierManager manager;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private OrderCreateManager orderCreateManager;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getPageCreateOrder() {
        User user = mock(User.class);
        Cart cart = mock(Cart.class);
        CartItem cartItem = mock(CartItem.class);
        List<CartItem> cartItems = List.of(cartItem);

        UserResponse userDTO = new UserResponse(
                1L, "Test User", "test@example.com",
                Role.USER, "1234567890"
        );

        CartItemResponse cartItemDTO = new CartItemResponse(
                1L, 2L, "Product Name",
                "image.jpg",
                BigDecimal.valueOf(50.0), 1
        );
        List<CartItemResponse> cartItemDTOs = List.of(cartItemDTO);

        when(userService.getCurrentUserCart()).thenReturn(user);
        when(user.getCart()).thenReturn(cart);
        when(cart.getCartItems()).thenReturn(cartItems);
        when(cart.totalPrice()).thenReturn(BigDecimal.valueOf(100.0));
        when(cart.getQuantity()).thenReturn(2);
        when(cartMapper.toListCartItemDto(cartItems)).thenReturn(cartItemDTOs);
        when(userMapper.convertEntityToDto(user)).thenReturn(userDTO);


        CreateOrderPageDTO result = orderService.getPageCreateOrder();

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100.0), result.totalPrice());
        assertEquals(2, result.totalItems());
        assertEquals(userDTO, result.user());
        assertEquals(1, result.cartItems().size());
    }

    @Test
    void getAll() {
        List<Order> expectedOrders = List.of(mock(Order.class));

        when(orderRepository.findAll()).thenReturn(expectedOrders);

        List<Order> result = orderService.getAll();

        assertEquals(expectedOrders, result);
        verify(orderRepository).findAll();
    }

    @Test
    void getById() {
        Order expectedOrder = mock(Order.class);

        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(expectedOrder));

        Order result = orderService.getById(1L);

        assertEquals(expectedOrder, result);
        verify(orderRepository).findById(1L);
    }


}