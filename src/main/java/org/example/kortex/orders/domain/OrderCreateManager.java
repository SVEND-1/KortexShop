package org.example.kortex.orders.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.domain.CartService;
import org.example.kortex.notify.event.NotifyEvent;
import org.example.kortex.notify.event.NotifyType;
import org.example.kortex.notify.kafka.NotifyKafkaProducer;
import org.example.kortex.orders.api.mapper.OrderMapper;
import org.example.kortex.orders.api.dto.OrderResponseDTO;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.orders.db.OrderRepository;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.domain.ProductService;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OrderCreateManager {
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductService productService;
    private final OrderItemService orderItemService;
    private final OrderMapper orderMapper;
    private final NotifyKafkaProducer kafkaProducer;

    @Autowired
    public OrderCreateManager(UserService userService, OrderRepository orderRepository,
                              CartService cartService, ProductService productService,
                              OrderItemService orderItemService, OrderMapper orderMapper,
                              NotifyKafkaProducer notifyKafkaProducer) {
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productService = productService;
        this.orderItemService = orderItemService;
        this.orderMapper = orderMapper;
        this.kafkaProducer = notifyKafkaProducer;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OrderResponseDTO createOrderFromCart() {
        log.info("Создания заказа из корзины");
        try {
            User user = userService.getCurrentUser();

            Cart cart = user.getCart();
            if (cart == null) {
                log.warn("Корзина не найдена для пользователя с id={}", user.getId());
                throw new RuntimeException("Корзина не найдена для пользователя с ID: " + user.getId());
            }

            if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                log.warn("Корзина пуста, невозможно создать заказ");
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
            NotifyEvent notifyEvent = new NotifyEvent(
                    user.getEmail(),
                    Map.of("userName", user.getName()),
                    NotifyType.ORDER_CREATED
            );
            kafkaProducer.sendMessageToKafka(notifyEvent);

            log.info("Заказ создан id={}", finalOrder.getId());

            return orderMapper.toDto(finalOrder);
        }
        catch (DataIntegrityViolationException e) {
            log.error("Нарушение целостности данных при создании заказа, ex={}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Конфликт данных. Возможно, недостаточно товара на складе"
            );
        }
        catch (Exception e){
            log.error("Ошибка при создании заказа, ex={}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Внутренняя ошибка сервера при создании заказа"
            );
        }
    }


    private void validateCartItems(Cart cart) {
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (product == null) {
                log.warn("Товар не найден в корзине");
                throw new RuntimeException("Товар не найден в корзине");
            }

            Product actualProduct = productService.getById(product.getId());
            if(actualProduct == null) {
                log.warn("Товар не найден id={}",product.getId());
                throw new RuntimeException("Товар не найден: " + product.getName());
            }

            if (actualProduct.getCount() < cartItem.getQuantity()) {
                log.error("Недостаточно товара на складе productId={}.Доступно: {}, а запрошено: {}",
                        product.getId(), actualProduct.getCount(), cartItem.getQuantity());
                throw new RuntimeException("Недостаточно товара на складе");
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
        try {
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
        }catch (Exception e){
            log.error("Ошибка создание элементов заказа cartId={},orderId={},ex={}",cart.getId(),order.getId(), e.getMessage());
            return null;
        }
    }
}
