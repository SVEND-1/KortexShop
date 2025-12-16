package org.example.kortex.orders.domain;

import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.domain.CartService;
import org.example.kortex.orders.api.OrdersSearchCourierFilter;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.orders.db.OrderRepository;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.domain.ProductService;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserService userService;
    @Mock
    private CartService cartService;
    @Mock
    private
    ProductService productService;
    @Mock private OrderItemService orderItemService;
    @InjectMocks
    private OrderService orderService;

    @Test
    void getAll() {
        Order order = new Order();
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Order> result = orderService.getAll();

        assertEquals(1, result.size());
    }

    @Test
    void getByIdWithItems() {
        Long orderId = 1L;
        Order order = new Order();

        when(orderRepository.findByIdWithItems(orderId))
                .thenReturn(order);

        Order result = orderService.getByIdWithItems(orderId);

        assertNotNull(result);
    }

    @Test
    void getById() {
        Long orderId = 1L;
        Order order = new Order();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        Order result = orderService.getById(orderId);

        assertNotNull(result);
    }

    @Test
    void setCourier() {
        Order order = new Order();
        Long courierId = 1L;
        User courier = new User();
        courier.setRole(User.Role.COURIER);

        when(userService.getById(courierId)).thenReturn(courier);
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.setCourier(order, courierId);

        assertNotNull(result);
    }

    @Test
    void setStatus() {
        Order order = new Order();
        order.setId(1L);

        when(orderRepository.findByIdWithItems(1L))
                .thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.setStatus(order, Order.OrderStatus.RETURNED);

        assertNotNull(result);
    }

    @Test
    void getOrdersByUserId() {
        Long userId = 1L;
        when(orderRepository.findOrderByUserId(userId))
                .thenReturn(List.of(new Order()));

        List<Order> result = orderService.getOrdersByUserId(userId);

        assertFalse(result.isEmpty());
    }

    @Test
    void createOrderFromCart() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setCart(null);

        when(userService.getById(userId)).thenReturn(user);


        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(userId,"ААА","Вввв");
        });

        assertTrue(exception.getMessage().contains("Корзина не найдена"));
        verify(cartService, never()).clearCartByUserId(anyLong());
    }


    @Test
    void assignedCourierOrders() {
        Long courierId = 1L;
        User courier = new User();
        courier.setId(courierId);
        courier.setRole(User.Role.COURIER);

        Order order1 = new Order();
        order1.setId(1L);
        order1.setCourier(courier);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setCourier(courier);

        when(userService.getById(courierId)).thenReturn(courier);
        when(orderRepository.assignedOrders(courierId))
                .thenReturn(Arrays.asList(order1, order2));

        List<Order> result = orderService.assignedCourierOrders(courierId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userService, times(1)).getById(courierId);
        verify(orderRepository, times(1)).assignedOrders(courierId);
    }

//    @Test
//    void assignedCourierOrdersPage_ShouldReturnPagedOrders() {
//        Long courierId = 1L;
//        User courier = new User();
//        courier.setId(courierId);
//        courier.setRole(User.Role.COURIER);
//
//        OrdersSearchCourierFilter filter = new OrdersSearchCourierFilter(courierId, 10, 0);
//
//        Order order1 = new Order();
//        order1.setId(1L);
//        Order order2 = new Order();
//        order2.setId(2L);
//
//        when(userService.getById(courierId)).thenReturn(courier);
//        when(orderRepository.assignedOrdersPage(eq(courierId), any(Pageable.class)))
//                .thenReturn(Arrays.asList(order1, order2));
//
//        List<Order> result = orderService.assignedCourierOrdersPage(filter);
//
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        verify(orderRepository, times(1)).assignedOrdersPage(eq(courierId), any(Pageable.class));
//    }
//
//    @Test
//    void availableCourierOrdersPage_ShouldReturnAvailableOrders() {
//        Order order1 = new Order();
//        order1.setId(1L);
//        order1.setCourier(null);
//        order1.setStatus(Order.OrderStatus.PENDING);
//
//        Order order2 = new Order();
//        order2.setId(2L);
//        order2.setCourier(null);
//        order2.setStatus(Order.OrderStatus.PENDING);
//
//        when(orderRepository.availableOrdersPage(any(Pageable.class)))
//                .thenReturn(Arrays.asList(order1, order2));
//
//        List<Order> result = orderService.availableCourierOrdersPage(10, 0);
//
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        verify(orderRepository, times(1)).availableOrdersPage(any(Pageable.class));
//    }
}