package org.example.kortex.users.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.notify.event.NotifyEvent;
import org.example.kortex.notify.event.NotifyType;
import org.example.kortex.notify.kafka.NotifyKafkaProducer;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.domain.OrderService;
import org.example.kortex.roleRequest.db.RoleRequest;
import org.example.kortex.users.api.dto.courier.CourierAssignResponse;
import org.example.kortex.users.api.dto.courier.SetStatusOrderResponse;
import org.example.kortex.users.domain.exception.CourierHasActiveOrderException;
import org.example.kortex.users.db.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


@Slf4j
@Service
public class CourierService {
    private final OrderService orderService;
    private final UserService userService;
    private final NotifyKafkaProducer kafkaProducer;

    @Autowired
    public CourierService(OrderService orderService, UserService userService, NotifyKafkaProducer notifyKafkaProducer) {
        this.orderService = orderService;
        this.userService = userService;
        this.kafkaProducer = notifyKafkaProducer;
    }

    //================================Controller Methods================================================

    public CourierAssignResponse assignOrder(Long id) {
        try {
            User courier = userService.getCurrentUser();

            if(isValidAssignOrder(courier.getId())){
                throw new CourierHasActiveOrderException("Невозможно взять новый заказ у курьера уже есть активный заказ в доставке.");
            }
            Order updateOrder = orderService.setCourier(orderService.getById(id), courier.getId());

            notify(updateOrder,Map.of(
                    "userName", updateOrder.getUser().getName(),
                    "courierName",updateOrder.getCourier().getName()));

            log.info("Курьер с id={} взял заказ с id={}" , courier.getId(), updateOrder.getId());
            return new CourierAssignResponse(
                    updateOrder.getId(),
                    courier.getId(),
                    courier.getName()
            );
        }catch (IllegalStateException e) {
            log.warn("Невозможно взять заказ: {}", e.getMessage());
            throw new IllegalStateException("Невозможно взять заказ: " + e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка при взятии заказа курьером: {}", e.getMessage());
            throw new RuntimeException("Ошибка при взятии заказа: " + e.getMessage());
        }
    }

    public SetStatusOrderResponse setStatus(Long id, Order.OrderStatus status) {
        try {
            Order orderUpdate = orderService.setStatus(orderService.getById(id), status);
            if(status.equals(Order.OrderStatus.DELIVERED_TO_DESTINATION)) {
                notify(orderUpdate, Map.of("userName", orderUpdate.getUser().getName()));
            }
            return new SetStatusOrderResponse(
                    orderUpdate.getId(),
                    status.name()
            );
        }
        catch (Exception e) {
            log.error("Ошибка при изменении статуса заказа: {}", e.getMessage());
            throw new RuntimeException("Ошибка при изменении статуса заказа: " + e.getMessage(), e);
        }
    }


    //================================Service Methods================================================

    private boolean isValidAssignOrder(Long courierId) {
        for (Order order : orderService.assignedCourierOrders(courierId)) {
            if(order.getStatus() == Order.OrderStatus.PENDING || order.getStatus() == Order.OrderStatus.DISPATCHED) {
                log.warn("Невозможно взять новый заказ: у курьера уже есть активный заказ в доставке." +
                        "ID активного заказа={}", order.getId());
                return true;
            }
        }
        return false;
    }

    private void notify(Order order, Map<String,String> message) {
        NotifyEvent notifyEvent = new NotifyEvent(
                order.getUser().getEmail(),
                message,
                NotifyType.COURIER_BRING_ORDER
        );
        kafkaProducer.sendMessageToKafka(notifyEvent);
    }
}
