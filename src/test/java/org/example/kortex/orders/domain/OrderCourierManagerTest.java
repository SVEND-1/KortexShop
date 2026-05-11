package org.example.kortex.orders.domain;

import org.example.kortex.orders.api.dto.OrdersSearchCourierFilter;
import org.example.kortex.orders.api.dto.OrderPageResponse;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.orders.db.OrderRepository;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.domain.ProductService;
import org.example.kortex.users.domain.mapper.CourierDTOMapper;
import org.example.kortex.users.api.dto.courier.CourierOrderDTO;
import org.example.kortex.users.api.dto.courier.CourierOrderItemDTO;
import org.example.kortex.users.api.dto.courier.CustomerInfoDTO;
import org.example.kortex.users.db.Role;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCourierManagerTest {

//    @Mock
//    private OrderRepository orderRepository;
//
//    @Mock
//    private UserService userService;
//
//    @Mock
//    private ProductService productService;
//
//    @Mock
//    private CourierDTOMapper courierDTOMapper;
//
//    @InjectMocks
//    private OrderCourierManager orderCourierManager;
//
//    @Test
//    void assignedCourierOrders() {
//        User courier = mock(User.class);
//        List<Order> expectedOrders = List.of(mock(Order.class));
//
//        when(userService.getById(1L)).thenReturn(courier);
//        when(courier.getRole()).thenReturn(Role.COURIER);
//        when(orderRepository.assignedOrders(1L)).thenReturn(expectedOrders);
//
//        List<Order> result = orderCourierManager.assignedCourierOrders(1L);
//
//        assertEquals(expectedOrders, result);
//        verify(userService).getById(1L);
//        verify(orderRepository).assignedOrders(1L);
//    }
//
//    @Test
//    void assignedCourierOrdersPage() {
//        OrdersSearchCourierFilter filter = new OrdersSearchCourierFilter(1L, 10, 0);
//        User courier = mock(User.class);
//        Page<Order> page = new PageImpl<>(List.of(mock(Order.class)));
//
//        CustomerInfoDTO customerInfo = new CustomerInfoDTO(
//                1L,
//                "Test Customer",
//                "test@example.com"
//        );
//
//        CourierOrderItemDTO orderItem = new CourierOrderItemDTO(
//                1L,
//                "Product Name",
//                "PROD-001",
//                "image.jpg",
//                "Product description",
//                2,
//                BigDecimal.valueOf(50.0),
//                BigDecimal.valueOf(100.0)
//        );
//
//        CourierOrderDTO courierOrderDTO = new CourierOrderDTO(
//                1L,
//                LocalDateTime.now(),
//                Order.OrderStatus.PENDING,
//                BigDecimal.valueOf(100.0),
//                "Shipping Address",
//                "Customer message",
//                customerInfo,
//                List.of(orderItem)
//        );
//
//        OrderPageResponse expectedResponse = new OrderPageResponse(
//                List.of(courierOrderDTO),
//                0, 10, 1L, 1, true, true, false
//        );
//
//        when(userService.getById(1L)).thenReturn(courier);
//        when(courier.getRole()).thenReturn(Role.COURIER);
//        when(orderRepository.assignedOrdersPage(eq(1L), any(Pageable.class))).thenReturn(page);
//        when(courierDTOMapper.toPageResponse(page)).thenReturn(expectedResponse);
//
//        OrderPageResponse result = orderCourierManager.assignedCourierOrdersPage(filter);
//
//        assertEquals(expectedResponse, result);
//        assertEquals(1, result.content().size());
//        assertEquals(1L, result.content().get(0).id());
//        assertEquals("Shipping Address", result.content().get(0).shippingAddress());
//        assertEquals("Test Customer", result.content().get(0).customer().fullName());
//        assertEquals("PROD-001", result.content().get(0).orderItems().get(0).productCode());
//        verify(userService).getById(1L);
//        verify(orderRepository).assignedOrdersPage(eq(1L), any(Pageable.class));
//        verify(courierDTOMapper).toPageResponse(page);
//    }
//
//    @Test
//    void availableCourierOrdersPage() {
//        Page<Order> page = new PageImpl<>(List.of(mock(Order.class)));
//
//        CustomerInfoDTO customerInfo = new CustomerInfoDTO(
//                2L,
//                "Another Customer",
//                "customer@example.com"
//        );
//
//        CourierOrderItemDTO orderItem = new CourierOrderItemDTO(
//                2L,
//                "Another Product",
//                "PROD-002",
//                "image2.jpg",
//                "Another description",
//                3,
//                BigDecimal.valueOf(75.0),
//                BigDecimal.valueOf(225.0)
//        );
//
//        CourierOrderDTO courierOrderDTO = new CourierOrderDTO(
//                2L,
//                LocalDateTime.now().minusDays(1),
//                Order.OrderStatus.DISPATCHED,
//                BigDecimal.valueOf(225.0),
//                "Different Address",
//                "Another message",
//                customerInfo,
//                List.of(orderItem)
//        );
//
//        OrderPageResponse expectedResponse = new OrderPageResponse(
//                List.of(courierOrderDTO),
//                0, 20, 5L, 1, true, false, false
//        );
//
//        when(orderRepository.availableOrdersPage(any(Pageable.class))).thenReturn(page);
//        when(courierDTOMapper.toPageResponse(page)).thenReturn(expectedResponse);
//
//        OrderPageResponse result = orderCourierManager.availableCourierOrdersPage(20, 0);
//
//        assertEquals(expectedResponse, result);
//        assertEquals(1, result.content().size());
//        assertEquals(2L, result.content().get(0).id());
//        assertEquals(BigDecimal.valueOf(225.0), result.content().get(0).totalAmount());
//        assertEquals("Another Customer", result.content().get(0).customer().fullName());
//        assertEquals("PROD-002", result.content().get(0).orderItems().get(0).productCode());
//        verify(orderRepository).availableOrdersPage(argThat(pageable ->
//                pageable.getPageSize() == 20 && pageable.getPageNumber() == 0
//        ));
//        verify(courierDTOMapper).toPageResponse(page);
//    }
//
//    @Test
//    void setCourier() {
//        Order order = mock(Order.class);
//        User courier = mock(User.class);
//        Order savedOrder = mock(Order.class);
//
//        when(savedOrder.getCourier()).thenReturn(courier);
//        when(courier.getId()).thenReturn(2L);
//
//        when(userService.getById(2L)).thenReturn(courier);
//        when(courier.getRole()).thenReturn(Role.COURIER);
//        when(orderRepository.save(order)).thenReturn(savedOrder);
//
//        Order result = orderCourierManager.setCourier(order, 2L);
//
//        assertNotNull(result);
//        assertEquals(savedOrder, result);
//        assertEquals(2L, result.getCourier().getId());
//        verify(userService).getById(2L);
//        verify(order).setCourier(courier);
//        verify(orderRepository).save(order);
//    }
//
//    @Test
//    void setStatus() {
//        Order order = mock(Order.class);
//        Order savedOrder = mock(Order.class);
//
//        when(orderRepository.save(order)).thenReturn(savedOrder);
//
//        Order result = orderCourierManager.setStatus(order, Order.OrderStatus.DISPATCHED);
//
//        assertEquals(savedOrder, result);
//        verify(order).setStatus(Order.OrderStatus.DISPATCHED);
//        verify(orderRepository).save(order);
//    }
//
//    @Test
//    void setStatus_Returned() {
//        Order order = mock(Order.class);
//        Order orderWithItems = mock(Order.class);
//        OrderItem orderItem = mock(OrderItem.class);
//        Product product = mock(Product.class);
//        Order savedOrder = mock(Order.class);
//
//        when(order.getId()).thenReturn(100L);
//        when(orderRepository.findByIdWithItems(100L)).thenReturn(orderWithItems);
//        when(orderWithItems.getOrderItems()).thenReturn(List.of(orderItem));
//        when(orderItem.getProduct()).thenReturn(product);
//        when(product.getId()).thenReturn(10L);
//        when(orderItem.getQuantity()).thenReturn(3);
//        when(orderRepository.save(order)).thenReturn(savedOrder);
//
//        Order result = orderCourierManager.setStatus(order, Order.OrderStatus.RETURNED);
//
//        assertEquals(savedOrder, result);
//        verify(productService).productAddQuantity(10L, 3);
//        verify(order).setStatus(Order.OrderStatus.RETURNED);
//        verify(orderRepository).save(order);
//    }

}