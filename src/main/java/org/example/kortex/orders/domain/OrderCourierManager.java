package org.example.kortex.orders.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.orders.api.dto.OrdersSearchCourierFilter;
import org.example.kortex.orders.api.dto.OrderPageResponse;
import org.example.kortex.orders.domain.exception.UserNotCourierException;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.orders.db.OrderRepository;
import org.example.kortex.products.domain.ProductService;
import org.example.kortex.users.domain.mapper.CourierDTOMapper;
import org.example.kortex.users.db.Role;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class OrderCourierManager {
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;
    private final CourierDTOMapper courierDTOMapper;

    public OrderCourierManager(OrderRepository orderRepository, UserService userService, ProductService productService, CourierDTOMapper courierDTOMapper) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productService = productService;
        this.courierDTOMapper = courierDTOMapper;
    }

    public List<Order> assignedCourierOrders(Long userId) {
        try {
            User courier = userService.getById(userId);
            validateCourier(courier);
            return orderRepository.assignedOrders(userId);
        }catch (Exception e){
            log.error("Не удалось загрузить заказы курьера,ex={}",e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public OrderPageResponse assignedCourierOrdersPage(OrdersSearchCourierFilter filter) {
        try {
            User courier = userService.getCurrentUser();
            validateCourier(courier);

            int pageSize = filter.pageSize() != null ? filter.pageSize() : 8;
            int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
            Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

            var orders = orderRepository.assignedOrdersPage(courier.getId(), pageable);
            return courierDTOMapper.toPageResponse(orders);
        }catch (Exception e){
            log.error("Не удалось загрузить заказы assignedCourierOrdersPage,ex={}",e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Transactional(readOnly = true)
    public OrderPageResponse availableCourierOrdersPage(Integer pageSize ,Integer pageNumber){
        try {
            pageSize = pageSize != null ? pageSize : 36;
            pageNumber = pageNumber != null ? pageNumber : 0;
            Pageable pageable = Pageable
                    .ofSize(pageSize)
                    .withPage(pageNumber);

            var orders = orderRepository.availableOrdersPage(pageable);
            return courierDTOMapper.toPageResponse(orders);
        }catch (Exception e){
            log.error("Не удалось загрузить доступные заказы,ex={}",e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Order setCourier(Order order,Long courierId) {
        try {
            User user = userService.getById(courierId);
            validateCourier(user);

            order.setCourier(user);
            Order saveOrder = orderRepository.save(order);
            log.info("На заказ orderId={} назначен курьер courierId={}" ,saveOrder.getId(), saveOrder.getCourier().getId());
            return saveOrder;
        }
        catch (Exception e){
            log.error("Не удалось назначить курьера courierId={},orderId={},ex={}",courierId,order.getId(),e.getMessage());
            return null;
        }
    }

    @Transactional
    public Order setStatus(Order order, Order.OrderStatus status) {
        try {
            if (status == Order.OrderStatus.CANCELLED) {
                order.setCourier(null);
                status = Order.OrderStatus.PENDING;
                log.warn("Курьер отказался от заказа, id={}", order.getId());
            }
            if (status == Order.OrderStatus.RETURNED) {
                returningProductsToBack(order.getId());
            }

            order.setStatus(status);
            return orderRepository.save(order);
        }
        catch (Exception e){
            log.error("Не удалось изменить статус заказа id={},ex={}",order.getId(),e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void returningProductsToBack(Long orderId) {
        try {
            Order orderWithItems = orderRepository.findByIdWithItems(orderId);
            for (OrderItem orderItem : orderWithItems.getOrderItems()) {
                productService.productAddQuantity(orderItem.getProduct().getId(), orderItem.getQuantity());
            }
        }catch (Exception e){
            log.error("Ошибка возврата заказа на склад orderId={},ex={}",orderId,e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void validateCourier(User user) {
        if (user.getRole() != Role.COURIER) {
            log.warn("Пользователь id={} не является курьером",user.getId());
            throw new UserNotCourierException(
                    String.format("Пользователь с ID %d не является курьером", user.getId())
            );
        }
    }
}
