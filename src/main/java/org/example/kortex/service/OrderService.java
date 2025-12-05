package org.example.kortex.service;

import javax.persistence.EntityNotFoundException;
import org.example.kortex.entity.*;
import org.example.kortex.repository.OrderRepository;
import org.example.kortex.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

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

    public Order getById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("не найден"));
    }


    public Order setStatus(Order order, Order.OrderStatus status) {
        if(status == Order.OrderStatus.CANCELLED) {
            order.setCourier(null);
            status = Order.OrderStatus.PENDING;
        }
        if(status == Order.OrderStatus.RETURNED) {
            List<OrderItem> orderItems = order.getOrderItems();
            for(OrderItem orderItem : orderItems) {
                productService.productAddQuantity(orderItem.getProduct().getId(),orderItem.getQuantity());
            }
        }
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findOrderByUserId(userId);
    }

    public Order createOrderFromCart(Long userId) {
        User user = userService.getById(userId);

        Cart cart = cartService.getCartByUserId(userId);
        if (cart == null) {
            throw new RuntimeException("Корзина не найдена для пользователя с ID: " + userId);
        }

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
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

        return finalOrder;
    }

    private void validateCartItems(Cart cart) {

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (product == null) {
                throw new RuntimeException("Товар не найден в корзине");
            }

            Product actualProduct = productService.getById(product.getId());
            if(actualProduct == null) {
                new RuntimeException("Товар не найден: " + product.getName());
            }

            if (actualProduct.getCount() < cartItem.getQuantity()) {
                throw new RuntimeException("Недостаточно товара на складе: " + actualProduct.getName() +
                        ". Доступно: " + actualProduct.getCount() +
                        ", запрошено: " + cartItem.getQuantity());
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

        List<OrderItem> savedItems = orderItemService.saveAll(orderItems);

        return savedItems;
    }



    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public Order update(Long id, Order orderToUpdate) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
        Order updatedOrder = new Order(
                order.getId(),
                orderToUpdate.getUser(),
                orderToUpdate.getCourier(),
                orderToUpdate.getStatus(),
                orderToUpdate.getTotalAmount(),
                order.getOrderItems());
        return orderRepository.save(updatedOrder);
    }

    public void deleted(Long id) {
        if(!orderRepository.existsById(id)){
            throw new NoSuchElementException("Заказ не найден");
        }
        orderRepository.deleteById(id);
    }

}
