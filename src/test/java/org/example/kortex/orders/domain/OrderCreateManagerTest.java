package org.example.kortex.orders.domain;

import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.domain.CartService;
import org.example.kortex.notify.event.NotifyEvent;
import org.example.kortex.notify.event.NotifyType;
import org.example.kortex.notify.kafka.NotifyKafkaProducer;
import org.example.kortex.orders.api.dto.OrderItemDTO;
import org.example.kortex.orders.api.dto.OrderResponseDTO;
import org.example.kortex.orders.api.mapper.OrderMapper;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.orders.db.OrderRepository;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.domain.ProductService;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCreateManagerTest {

    @Mock
    private UserService userService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ProductService productService;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private NotifyKafkaProducer kafkaProducer;

    @InjectMocks
    private OrderCreateManager orderCreateManager;

    @Test
    void createOrderFromCart() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getEmail()).thenReturn("test@example.com");
        when(user.getName()).thenReturn("Test User");

        Cart cart = mock(Cart.class);
        CartItem cartItem = mock(CartItem.class);
        Product product = mock(Product.class);
        Order order = mock(Order.class);
        Order finalOrder = mock(Order.class);
        OrderItem orderItem = mock(OrderItem.class);

        // Создаем реальный DTO объект
        OrderItemDTO orderItemDTO = new OrderItemDTO(
                "Product Name", 50.0, 2, "image.jpg"
        );
        OrderResponseDTO expectedDTO = new OrderResponseDTO(
                1L, List.of(orderItemDTO), LocalDate.now(),
                BigDecimal.valueOf(100.0), Order.OrderStatus.PENDING
        );

        when(userService.getCurrentUser()).thenReturn(user);
        when(user.getCart()).thenReturn(cart);
        when(cart.getCartItems()).thenReturn(List.of(cartItem));
        when(cartItem.getProduct()).thenReturn(product);
        when(product.getId()).thenReturn(1L);
        when(productService.getById(1L)).thenReturn(product);
        when(product.getCount()).thenReturn(10);
        when(cartItem.getQuantity()).thenReturn(2);
        when(product.getPrice()).thenReturn(BigDecimal.valueOf(50));

        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemService.saveAll(anyList())).thenReturn(List.of(orderItem));

        when(orderRepository.save(order)).thenReturn(finalOrder);

        when(finalOrder.getId()).thenReturn(1L);
        when(finalOrder.getOrderItems()).thenReturn(List.of(orderItem));
        when(orderItem.getProduct()).thenReturn(product);
        when(orderItem.getQuantity()).thenReturn(2);

        when(orderMapper.toDto(finalOrder)).thenReturn(expectedDTO);

        ArgumentCaptor<NotifyEvent> eventCaptor = ArgumentCaptor.forClass(NotifyEvent.class);

        OrderResponseDTO result = orderCreateManager.createOrderFromCart();

        assertEquals(expectedDTO, result);
        assertEquals(1L, result.orderId());
        assertEquals(BigDecimal.valueOf(100.0), result.totalAmount());
        assertEquals(1, result.items().size());

        verify(cartService).clearCartByUserId(1L);
        verify(productService).productSubtractQuantity(1L, 2);
        verify(kafkaProducer).sendMessageToKafka(eventCaptor.capture());

        NotifyEvent sentEvent = eventCaptor.getValue();
        assertEquals("test@example.com", sentEvent.email());
        assertEquals(NotifyType.ORDER_CREATED, sentEvent.type());
    }


}