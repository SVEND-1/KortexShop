package org.example.kortex.orders.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.domain.CartService;
import org.example.kortex.notify.event.NotifyEvent;
import org.example.kortex.notify.event.NotifyType;
import org.example.kortex.notify.kafka.NotifyKafkaProducer;
import org.example.kortex.orders.api.dto.OrderPaymentApproved;
import org.example.kortex.orders.domain.exception.ProductZeroException;
import org.example.kortex.orders.domain.mapper.OrderMapper;
import org.example.kortex.orders.api.dto.OrderResponseDTO;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.orders.db.OrderRepository;
import org.example.kortex.payments.api.dto.response.payment.PaymentCreateResponse;
import org.example.kortex.payments.api.dto.response.payment.PaymentResponse;
import org.example.kortex.payments.db.PaymentEntity;
import org.example.kortex.payments.domain.PaymentService;
import org.example.kortex.products.db.Product;
import org.example.kortex.products.domain.ProductService;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.loolzaaa.youkassa.model.PersonalData;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
public class OrderCreateManager {
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductService productService;
    private final OrderItemService orderItemService;
    private final NotifyKafkaProducer kafkaProducer;
    private final PaymentService paymentService;

    @Autowired
    public OrderCreateManager(UserService userService, OrderRepository orderRepository,
                              CartService cartService, ProductService productService,
                              OrderItemService orderItemService,
                              NotifyKafkaProducer notifyKafkaProducer, PaymentService paymentService) {
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productService = productService;
        this.orderItemService = orderItemService;
        this.kafkaProducer = notifyKafkaProducer;
        this.paymentService = paymentService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public PaymentCreateResponse createOrderFromCart(String comment) {
        try {
            User user = userService.getCurrentUser();
            Cart cart = user.getCart();

            isValid(user,cart);
            validateCartItems(cart);

            BigDecimal total = calculateTotalAmount(cart);//TODO ИСПРАВИТЬ МАТЕМАТИКУ
            Order order = Order.builder()
                    .user(user)
                    .status(Order.OrderStatus.PENDING)//TODO РАЗОБРАТЬСЯ С ВЕБХУК И ДОПИСАТЬ
                    .shippingAddress(user.getAddress())
                    .message(comment)
                    .totalAmount(total)
                    .build();
            Order savedOrder = orderRepository.save(order);//Переписать логику

            List<OrderItem> orderItems = createOrderItemsFromCart(cart, savedOrder);
            savedOrder.setOrderItems(orderItems);

            cartService.clearCartByUserId(user.getId());
            Order paymentOrder = productSubtractQuantity(savedOrder);

            BigDecimal yookassaAmount = total.setScale(2, RoundingMode.HALF_UP);
            String value = yookassaAmount.toPlainString();
            PaymentCreateResponse response = paymentService.createPayment(value,paymentOrder.getId());
            paymentOrder.setPaymentId(response.paymentId());
            orderRepository.save(paymentOrder);

            notify(user);
            return response;
        }
        catch (DataIntegrityViolationException e) {
            log.error("Нарушение целостности данных при создании заказа, ex={}", e.getMessage());
            throw new RuntimeException(
                    "Конфликт данных. Возможно, недостаточно товара на складе"
            );
        }
        catch (Exception e){
            log.error("Ошибка при создании заказа, ex={}", e.getMessage());
            throw new RuntimeException(
                    "Внутренняя ошибка сервера при создании заказа"
            );
        }
    }

    @Transactional
    public String paymentApprove(OrderPaymentApproved request){//TODO добавить id платежа в Order
        try {
            String validationError = validatePayment(request.paymentId());
            if (validationError != null) {
                return validationError;
            }
            Order order = orderRepository.findById(request.orderId()).orElseThrow(() -> new EntityNotFoundException("Не найден заказ"));
            order.setStatus(Order.OrderStatus.PENDING);
            orderRepository.save(order);
            return "Успешно";
        }catch (Exception e){
            log.error("Не получилось подтвердить оплату");
            throw new RuntimeException(e);
        }
    }

    private List<OrderItem> createOrderItemsFromCart(Cart cart, Order order) {
        try {
            List<OrderItem> orderItems = new ArrayList<>();

            for (CartItem cartItem : cart.getCartItems()) {
                BigDecimal itemPrice = cartItem.getProduct().getPrice();
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .price(itemPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                        .build();
                orderItems.add(orderItem);
            }

            return orderItemService.saveAll(orderItems);
        }catch (Exception e){
            log.error("Ошибка создание элементов заказа cartId={},orderId={},ex={}",cart.getId(),order.getId(), e.getMessage());
            throw new RuntimeException("Ошибка создание элементов заказа",e);
        }
    }

    private Order productSubtractQuantity(Order savedOrder){
        Order finalOrder = orderRepository.save(savedOrder);
        for (OrderItem orderItem : finalOrder.getOrderItems()) {
            productService.productSubtractQuantity(orderItem.getProduct().getId(), orderItem.getQuantity());
        }
        return finalOrder;
    }

    private void notify(User user){
        NotifyEvent notifyEvent = new NotifyEvent(
                user.getEmail(),
                Map.of("userName", user.getName()),
                NotifyType.ORDER_CREATED
        );
        kafkaProducer.sendMessageToKafka(notifyEvent);
    }

    private BigDecimal calculateTotalAmount(Cart cart) {
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getCartItems()) {
            BigDecimal itemPrice = cartItem.getProduct().getPrice();
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(itemTotal);
        }

        return total;
    }

    private String validatePayment(String paymentId) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            return "Неверный paymentId";
        }
        paymentService.isValidUser(paymentId);

        PaymentResponse payment = paymentService.findPaymentDto(paymentId);
        if (!"succeeded".equals(payment.status())) {
            return "Платёж не прошёл";
        }
        return null;
    }

    private void validateCartItems(Cart cart) {
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (product == null) {
                log.warn("Товар не найден в корзине");
                throw new NoSuchElementException("Товар не найден в корзине");
            }

            Product actualProduct = productService.getByIdEntity(product.getId());
            if(actualProduct == null) {
                log.warn("Товар не найден id={}",product.getId());
                throw new NoSuchElementException("Товар не найден: " + product.getName());
            }

            if (actualProduct.getCount() < cartItem.getQuantity()) {
                log.error("Недостаточно товара на складе productId={}.Доступно: {}, а запрошено: {}",
                        product.getId(), actualProduct.getCount(), cartItem.getQuantity());
                throw new ProductZeroException("Недостаточно товара на складе");
            }
        }
    }

    private void isValid(User user,Cart cart) {
        if(user.getAddress() == null){
            log.warn("Для создание заказа необходим адрес");
            throw new NoSuchElementException("Для создание заказа необходим адрес");
        }

        if (cart == null) {
            log.warn("Корзина не найдена для пользователя с id={}", user.getId());
            throw new NoSuchElementException("Корзина не найдена для пользователя с ID: " + user.getId());
        }

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {//TODO Добавить своё исключение
            log.warn("Корзина пуста, невозможно создать заказ");
            throw new RuntimeException("Корзина пуста, невозможно создать заказ");
        }
    }
}
