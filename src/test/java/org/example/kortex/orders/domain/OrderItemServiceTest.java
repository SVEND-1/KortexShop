package org.example.kortex.orders.domain;

import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.orders.db.OrderItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderItemService orderItemService;

    @Test
    void saveAll() {
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setId(1L);
        orderItem1.setQuantity(2);

        OrderItem orderItem2 = new OrderItem();
        orderItem2.setId(2L);
        orderItem2.setQuantity(3);

        List<OrderItem> orderItems = Arrays.asList(orderItem1, orderItem2);

        when(orderItemRepository.saveAll(orderItems)).thenReturn(orderItems);

        List<OrderItem> result = orderItemService.saveAll(orderItems);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderItemRepository, times(1)).saveAll(orderItems);
    }
}