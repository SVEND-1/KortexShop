package org.example.kortex.orders.domain;

import javax.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.orders.api.OrdersSearchCourierFilter;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderRepository;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.domain.ProductService;
import org.example.kortex.carts.domain.CartService;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final CartService cartService;
    private final ProductService productService;
    private final OrderItemService orderItemService;

    @Autowired
    public OrderService(OrderRepository orderRepository,@Lazy UserService userService,
                        CartService cartService, ProductService productService,OrderItemService orderItemService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.cartService = cartService;
        this.productService = productService;
        this.orderItemService = orderItemService;
    }

    public List<Order> getAll(){
        return orderRepository.findAll();
    }
    public Order getByIdWithItems(Long id) {
        return orderRepository.findByIdWithItems(id);
    }

    public List<Order> assignedCourierOrders(Long userId) {
        User courier = userService.getById(userId);
        validateCourier(courier);
        List<Order> orders = orderRepository.assignedOrders(userId);
        log.info("Заказы курьера выданы id курьера: " + userId);
        return orders;
    }

    public Page<Order> assignedCourierOrdersPage(Long userId, Integer pageSize, Integer pageNumber) {
        User courier = userService.getById(userId);
        validateCourier(courier);

        pageSize = pageSize != null ? pageSize : 8;
        pageNumber = pageNumber != null ? pageNumber : 0;
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        Page<Order> orders = orderRepository.assignedOrdersPage(userId, pageable);
        log.info("Заказы курьера с пагинацией выданы id курьера: " + userId);
        return orders;
    }

    public Page<Order> assignedCourierOrdersPage(OrdersSearchCourierFilter filter) {
        log.info("Заказы курьера с фильтром: " + filter);
        User courier = userService.getById(filter.userId());
        validateCourier(courier);

        int pageSize = filter.pageSize() != null ? filter.pageSize() : 8;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        var orders = orderRepository.assignedOrdersPage(filter.userId(), pageable);
        log.info("Заказы курьера с фильтром выданы");
        return orders;
    }


    public Page<Order> availableCourierOrdersPage(Integer pageSize ,Integer pageNumber){
        log.info("Запрос доступный заказов для курьеров");
        pageSize = pageSize != null ? pageSize : 36;
        pageNumber = pageNumber != null ? pageNumber : 0;
        Pageable pageable = Pageable
                .ofSize(pageSize)
                .withPage(pageNumber);

        var orders = orderRepository.availableOrdersPage(pageable);
        log.info("Доступные заказы для курьеров выданы ");
        return orders;
    }


    public Order getById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("не найден"));
    }

    public Order setCourier(Order order,Long courierId) {
        log.info("Назначения курьера на заказ");
        User user = userService.getById(courierId);
        validateCourier(user);

        order.setCourier(user);
        Order saveOrder = orderRepository.save(order);
        log.info("На заказ " +  saveOrder.getId() + " назначен курьер: " + saveOrder.getCourier().getId());
        return saveOrder;
    }


    public Order setStatus(Order order, Order.OrderStatus status) {
        log.info("Изменения статуса заказа " + order.getId() + " на статсус " + status.name());
        if(status == Order.OrderStatus.CANCELLED) {
            log.info("Запрос курьера на отказ от заказа");
            order.setCourier(null);
            status = Order.OrderStatus.PENDING;
            log.info("Курьер отказался от заказа");
        }
        if(status == Order.OrderStatus.RETURNED) {
            log.info("Вернуть заказ");
            List<OrderItem> orderItems = order.getOrderItems();
            Order orderWithItems = getByIdWithItems(order.getId());
            for (OrderItem orderItem : orderWithItems.getOrderItems()) {
                productService.productAddQuantity(orderItem.getProduct().getId(), orderItem.getQuantity());
            }
            log.info("Заказ был возвращен");
        }
        order.setStatus(status);
        Order saveOrder = orderRepository.save(order);
        log.info("Статус заказа изменен");
        return saveOrder;
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findOrderByUserId(userId);
    }

    public Order createOrderFromCart(Long userId) {
        log.info("Создания заказа из корзины");
        User user = userService.getById(userId);

        Cart cart = user.getCart();
        if (cart == null) {
            log.error("Корзина не найдена для пользователя с ID: " + userId);
            throw new RuntimeException("Корзина не найдена для пользователя с ID: " + userId);
        }

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            log.error("Корзина пуста, невозможно создать заказ");
            throw new RuntimeException("Корзина пуста, невозможно создать заказ");
        }

        validateCartItems(cart);

        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);

        BigDecimal totalAmount = calculateTotalAmount(cart);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = createOrderItemsFromCart(cart, savedOrder);
        savedOrder.setOrderItems(orderItems);

        cartService.clearCartByUserId(userId);

        Order finalOrder = orderRepository.save(savedOrder);
        for(OrderItem orderItem : finalOrder.getOrderItems()) {
            productService.productSubtractQuantity(orderItem.getProduct().getId(),orderItem.getQuantity());
        }
        log.info("Заказ создан с id: " + finalOrder.getId());
        return finalOrder;
    }

    private void validateCourier(User user) {
        if (user.getRole() != User.Role.COURIER) {
            log.error("Пользователь с ID " + user.getId() + " не является курьером");
            throw new IllegalArgumentException(
                    String.format("Пользователь с ID %d не является курьером", user.getId())
            );
        }
    }

    private void validateCartItems(Cart cart) {

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (product == null) {
                log.error("Товар не найден в корзине");
                throw new RuntimeException("Товар не найден в корзине");
            }

            Product actualProduct = productService.getById(product.getId());
            if(actualProduct == null) {
                log.error("Товар не найден: " + product.getName());
                new RuntimeException("Товар не найден: " + product.getName());
            }

            if (actualProduct.getCount() < cartItem.getQuantity()) {
                String message = "Недостаточно товара на складе: " + actualProduct.getName() +
                        ". Доступно: " + actualProduct.getCount() +
                        ", запрошено: " + cartItem.getQuantity();
                log.error(message);
                throw new RuntimeException(message);
            }
        }
    }

    private BigDecimal calculateTotalAmount(Cart cart) {
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getCartItems()) {
            BigDecimal itemPrice = BigDecimal.valueOf(cartItem.getProduct().getPrice());
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(itemTotal);
        }

        return total;
    }

    private List<OrderItem> createOrderItemsFromCart(Cart cart, Order order) {
        log.info("Создания OrderItem");
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());

            BigDecimal itemPrice = BigDecimal.valueOf(cartItem.getProduct().getPrice());
            orderItem.setPrice(itemPrice);

            orderItem.setPrice(itemPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            orderItems.add(orderItem);
        }

        List<OrderItem> finalOrderItems = orderItemService.saveAll(orderItems);
        log.info("Созданы все OrderItem");
        return finalOrderItems;
    }

}
