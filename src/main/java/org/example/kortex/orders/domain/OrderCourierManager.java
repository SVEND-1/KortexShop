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
        User courier = userService.getById(userId);
        validateCourier(courier);
        List<Order> orders = orderRepository.assignedOrders(userId);
        log.info("Заказы курьера выданы courierId={}", userId);
        return orders;
    }

    @Transactional
    public OrderPageResponse assignedCourierOrdersPage(OrdersSearchCourierFilter filter) {
        log.info("Заказы курьера с filter={}", filter);

        User courier = userService.getCurrentUser();
        validateCourier(courier);

        int pageSize = filter.pageSize() != null ? filter.pageSize() : 8;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        var orders = orderRepository.assignedOrdersPage(courier.getId(), pageable);
        OrderPageResponse response = courierDTOMapper.toPageResponse(orders);
        log.info("Заказы курьера с фильтром выданы");
        return response;
    }

    public OrderPageResponse availableCourierOrdersPage(Integer pageSize ,Integer pageNumber){
        log.info("Запрос доступный заказов для курьеров");

        pageSize = pageSize != null ? pageSize : 36;
        pageNumber = pageNumber != null ? pageNumber : 0;
        Pageable pageable = Pageable
                .ofSize(pageSize)
                .withPage(pageNumber);

        var orders = orderRepository.availableOrdersPage(pageable);
        OrderPageResponse response = courierDTOMapper.toPageResponse(orders);
        log.info("Доступные заказы для курьеров выданы ");
        return response;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Order setCourier(Order order,Long courierId) {
        try {
            log.info("Назначения курьера на заказ");
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
            log.info("Изменения статуса заказа orderId={} на status={}", order.getId(), status.name());

            if (status == Order.OrderStatus.CANCELLED) {
                log.info("Запрос курьера на отказ от заказа");
                order.setCourier(null);
                status = Order.OrderStatus.PENDING;
                log.info("Курьер отказался от заказа");
            }

            if (status == Order.OrderStatus.RETURNED) {
                returningProductsToBack(order.getId());
            }

            order.setStatus(status);
            Order saveOrder = orderRepository.save(order);
            log.info("Статус заказа изменен");
            return saveOrder;
        }
        catch (Exception e){
            log.error("Не удалось изменить статус заказа id={},ex={}",order.getId(),e.getMessage());
            return null;
        }
    }

    private void returningProductsToBack(Long orderId) {
        try {
            log.info("Вернуть заказ");
            Order orderWithItems = orderRepository.findByIdWithItems(orderId);
            for (OrderItem orderItem : orderWithItems.getOrderItems()) {
                productService.productAddQuantity(orderItem.getProduct().getId(), orderItem.getQuantity());
            }
            log.info("Заказ был возвращен");
        }catch (Exception e){
            log.error("Ошибка возврата заказа на склад orderId={},ex={}",orderId,e.getMessage());
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
