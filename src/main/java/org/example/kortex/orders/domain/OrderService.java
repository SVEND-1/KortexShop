package org.example.kortex.orders.domain;

import javax.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.domain.mapper.CartMapper;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.orders.api.dto.CreateOrderPageDTO;
import org.example.kortex.orders.api.dto.OrdersSearchCourierFilter;
import org.example.kortex.orders.api.dto.OrderPageResponse;
import org.example.kortex.orders.api.dto.OrderResponseDTO;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderRepository;
import org.example.kortex.orders.domain.mapper.OrderMapper;
import org.example.kortex.users.domain.mapper.UserMapper;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final OrderCourierManager manager;
    private final CartMapper cartMapper;
    private final OrderCreateManager orderCreateManager;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;

    @Autowired
    public OrderService(OrderRepository orderRepository, @Lazy UserService userService,
                        OrderCourierManager manager,
                        CartMapper cartMapper,
                        OrderCreateManager orderCreateManager, UserMapper userMapper, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.manager = manager;
        this.cartMapper = cartMapper;
        this.orderCreateManager = orderCreateManager;
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
    }

    //================================Controller Methods================================================


    public OrderPageResponse assignedCourierOrdersPage(OrdersSearchCourierFilter filter) {
        return manager.assignedCourierOrdersPage(filter);
    }

    @Async("asyncExecutor")
    public CompletableFuture<OrderPageResponse> availableCourierOrdersPage(Integer pageSize ,Integer pageNumber){
        return CompletableFuture.completedFuture(
                manager.availableCourierOrdersPage(pageSize,pageNumber)
        );
    }

    public List<OrderResponseDTO> getHistoryOrders(){
        try {
            User user = userService.getCurrentUser();
            return orderMapper.toDtoListOrder(orderRepository.findOrdersWithItemsByUserEmail(user.getEmail()));
        }catch (Exception e) {
            log.error("Не получилось получить историю заказов,ex={}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public CreateOrderPageDTO getPageCreateOrder(){
        try {
            User user = userService.getCurrentUserCart();
            Cart cart = user.getCart();
            List<CartItem> cartItems = cart.getCartItems();

            return new CreateOrderPageDTO(
                    cartMapper.toListCartItemDto(cartItems),
                    cart.totalPrice(),
                    cart.getQuantity(),
                    userMapper.convertEntityToDto(user)
            );
        }catch (Exception e) {
            log.error("Ошибка при получении данных заказов, ex={}", e.getMessage());
            return null;
        }
    }

    public OrderResponseDTO createOrderFromCart(String comment) {
        return orderCreateManager.createOrderFromCart(comment);
    }

    //================================Service Methods================================================

    public List<Order> getAll(){
        return orderRepository.findAll();
    }

    public Order getById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("не найден"));
    }

    public List<Order> assignedCourierOrders(Long userId) {
        return manager.assignedCourierOrders(userId);
    }

    public Order setCourier(Order order,Long courierId) {
        return manager.setCourier(order,courierId);
    }

    public Order setStatus(Order order, Order.OrderStatus status) {
        return manager.setStatus(order,status);
    }

}
