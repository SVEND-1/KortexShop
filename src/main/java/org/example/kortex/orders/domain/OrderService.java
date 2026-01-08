package org.example.kortex.orders.domain;

import javax.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.api.dto.CartMapper;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.orders.api.dto.OrderMapper;
import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.orders.api.OrdersSearchCourierFilter;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderRepository;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.domain.ProductService;
import org.example.kortex.carts.domain.CartService;
import org.example.kortex.users.db.Role;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.EmailSenderService;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final CartService cartService;
    private final ProductService productService;
    private final OrderItemService orderItemService;
    private final EmailSenderService emailSenderService;
    private final OrderCourierManager manager;
    private final CartMapper cartMapper;
    private final OrderMapper orderMapper;

    @Autowired
    public OrderService(OrderRepository orderRepository, @Lazy UserService userService,
                        CartService cartService, ProductService productService, OrderItemService orderItemService,
                        EmailSenderService emailSenderService, OrderCourierManager manager,
                        CartMapper cartMapper, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.cartService = cartService;
        this.productService = productService;
        this.orderItemService = orderItemService;
        this.emailSenderService = emailSenderService;
        this.manager = manager;
        this.cartMapper = cartMapper;
        this.orderMapper = orderMapper;
    }

    public List<Order> getAll(){
        return orderRepository.findAll();
    }
    public Order getByIdWithItems(Long id) {
        return orderRepository.findByIdWithItems(id);
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
            log.error("Ошибка при получении данных заказов: " + e.getMessage());
            return error;
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Map<String, Object> createOrderFromCart() {//TODO Можно добавить менеджер создания заказа
        log.info("Создания заказа из корзины");
        try {
            User user = userService.getCurrentUser();

            Cart cart = user.getCart();
            if (cart == null) {
                log.error("Корзина не найдена для пользователя с ID: " + user.getId());
                throw new RuntimeException("Корзина не найдена для пользователя с ID: " + user.getId());
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

            cartService.clearCartByUserId(user.getId());

            Order finalOrder = orderRepository.save(savedOrder);
            for (OrderItem orderItem : finalOrder.getOrderItems()) {
                productService.productSubtractQuantity(orderItem.getProduct().getId(), orderItem.getQuantity());
            }
            emailSenderService.sendMessage(user.getEmail(), "Заказ успешно создан!"
                    , "Мы отравим вам письмо когда курьер возьмет его");
            log.info("Заказ создан с id: " + finalOrder.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("order", orderMapper.toDto(finalOrder));
            response.put("redirect","/");
            return response;
        }
        catch (DataIntegrityViolationException e) {
            log.error("Нарушение целостности данных при создании заказа: " + e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Конфликт данных. Возможно, недостаточно товара на складе"
            );
        }
        catch (Exception e){
            log.error("Ошибка при создании заказа: " + e.getMessage());
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Внутренняя ошибка сервера при создании заказа"
                );
        }
    }


    private void validateCartItems(Cart cart) {//TODO полностью логи переделать
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
