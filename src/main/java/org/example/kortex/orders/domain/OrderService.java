package org.example.kortex.orders.domain;

import javax.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.api.dto.CartMapper;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.orders.api.dto.OrderMapper;
import org.example.kortex.orders.api.OrdersSearchCourierFilter;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderRepository;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final OrderCourierManager manager;
    private final CartMapper cartMapper;
    private final OrderMapper orderMapper;
    private final OrderCreateManager orderCreateManager;

    @Autowired
    public OrderService(OrderRepository orderRepository, @Lazy UserService userService,
                        OrderCourierManager manager,
                        CartMapper cartMapper, OrderMapper orderMapper,
                        OrderCreateManager orderCreateManager) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.manager = manager;
        this.cartMapper = cartMapper;
        this.orderMapper = orderMapper;
        this.orderCreateManager = orderCreateManager;
    }

    public List<Order> getAll(){
        return orderRepository.findAll();
    }

    public Order getById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("не найден"));
    }


    public List<Order> assignedCourierOrders(Long userId) {
        return manager.assignedCourierOrders(userId);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Page<Order>> assignedCourierOrdersPage(OrdersSearchCourierFilter filter) {
        return CompletableFuture.completedFuture(manager.assignedCourierOrdersPage(filter));
    }

    @Async("asyncExecutor")
    public CompletableFuture<Page<Order>> availableCourierOrdersPage(Integer pageSize ,Integer pageNumber){
        return CompletableFuture.completedFuture(manager.availableCourierOrdersPage(pageSize,pageNumber));
    }

    public Order setCourier(Order order,Long courierId) {
        return manager.setCourier(order,courierId);
    }

    public Order setStatus(Order order, Order.OrderStatus status) {
        return manager.setStatus(order,status);
    }

    public Map<String,Object> getPageCreateOrder(){//TODO DTO
        try {
            User user = userService.getCurrentUserCart();
            Cart cart = user.getCart();
            List<CartItem> cartItems = cart.getCartItems();

            Map<String, Object> response = new HashMap<>();
            response.put("cartItems", cartMapper.toListCartItemDto(cartItems));
            response.put("totalPrice", cart.totalPrice());
            response.put("totalItems", cart.getQuantity());
            response.put("user", orderMapper.toUserOrderDto(user));

            return response;
        }catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Ошибка при получении данных заказов: " + e.getMessage());
            log.error("Ошибка при получении данных заказов, ex={}", e.getMessage());
            return error;
        }
    }


    public Map<String, Object> createOrderFromCart() {
        return orderCreateManager.createOrderFromCart();
    }



}
