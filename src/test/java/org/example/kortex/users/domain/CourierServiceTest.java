package org.example.kortex.users.domain;

import org.example.kortex.notify.event.NotifyEvent;
import org.example.kortex.notify.event.NotifyType;
import org.example.kortex.notify.kafka.NotifyKafkaProducer;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.domain.OrderService;
import org.example.kortex.users.api.dto.courier.CourierAssignResponse;
import org.example.kortex.users.api.dto.courier.SetStatusOrderResponse;
import org.example.kortex.users.api.exception.CourierHasActiveOrderException;
import org.example.kortex.users.db.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierServiceTest {

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    @Mock
    private NotifyKafkaProducer kafkaProducer;

    @InjectMocks
    private CourierService courierService;

    private User courier;
    private User customer;
    private Order order;

    @BeforeEach
    void setUp() {
        courier = new User();
        courier.setId(1L);
        courier.setName("Курьер");
        courier.setEmail("courier@example.com");


        customer = new User();
        customer.setId(2L);
        customer.setName("Заказчик");
        customer.setEmail("customer@example.com");

        order = new Order();
        order.setId(100L);
        order.setUser(customer);
        order.setStatus(Order.OrderStatus.PENDING);

    }

    @Test
    void assignOrder() {
        // Arrange
        Long orderId = 100L;
        when(userService.getCurrentUser()).thenReturn(courier);
        when(orderService.getById(orderId)).thenReturn(order);
        when(orderService.assignedCourierOrders(courier.getId())).thenReturn(List.of());

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setUser(customer);
        updatedOrder.setCourier(courier);
        updatedOrder.setStatus(Order.OrderStatus.PENDING);

        when(orderService.setCourier(order, courier.getId())).thenReturn(updatedOrder);

        // Act
        CourierAssignResponse response = courierService.assignOrder(orderId);

        // Assert
        assertNotNull(response);
        assertEquals(orderId, response.orderId());
        assertEquals(courier.getId(), response.courierId());
        assertEquals(courier.getName(), response.courierName());

        verify(userService).getCurrentUser();
        verify(orderService).getById(orderId);
        verify(orderService).assignedCourierOrders(courier.getId());
        verify(orderService).setCourier(order, courier.getId());
        verify(kafkaProducer).sendMessageToKafka(any(NotifyEvent.class));
    }



    @Test
    void setStatus() {
        // Arrange
        Long orderId = 100L;
        Order.OrderStatus newStatus = Order.OrderStatus.DISPATCHED;

        when(orderService.getById(orderId)).thenReturn(order);

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setUser(customer);
        updatedOrder.setCourier(courier);
        updatedOrder.setStatus(newStatus);

        when(orderService.setStatus(order, newStatus)).thenReturn(updatedOrder);

        // Act
        SetStatusOrderResponse response = courierService.setStatus(orderId, newStatus);

        // Assert
        assertNotNull(response);
        assertEquals(orderId, response.orderId());
        assertEquals(newStatus.name(), response.status());

        verify(orderService).getById(orderId);
        verify(orderService).setStatus(order, newStatus);
        verify(kafkaProducer, never()).sendMessageToKafka(any());
    }

}