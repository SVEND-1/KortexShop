package org.example.kortex.orders.domain;

import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.orders.db.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderItemService orderItemService;

    private OrderItem orderItem1;
    private OrderItem orderItem2;
    private List<OrderItem> orderItems;

    @BeforeEach
    void setUp() {
        orderItem1 = new OrderItem();
        orderItem1.setId(1L);
        orderItem1.setQuantity(2);
        orderItem1.setPrice(new BigDecimal("50.0"));

        orderItem2 = new OrderItem();
        orderItem2.setId(2L);
        orderItem2.setQuantity(5);
        orderItem2.setPrice(new BigDecimal("550.0"));

        orderItems = Arrays.asList(orderItem1, orderItem2);
    }

    @Test
    void saveAll() {
        List<OrderItem> savedItems = new ArrayList<>(orderItems);
        when(orderItemRepository.saveAll(orderItems)).thenReturn(savedItems);

        List<OrderItem> result = orderItemService.saveAll(orderItems);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(savedItems, result);

        verify(orderItemRepository).saveAll(orderItems);
    }
}