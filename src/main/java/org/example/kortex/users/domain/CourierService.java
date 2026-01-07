package org.example.kortex.users.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.orders.api.OrdersSearchCourierFilter;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.domain.OrderService;
import org.example.kortex.users.api.dto.courier.CourierAssignResponse;
import org.example.kortex.users.api.dto.courier.CourierDTOMapper;
import org.example.kortex.users.api.dto.courier.SetStatusOrderResponse;
import org.example.kortex.users.db.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
public class CourierService {
    private final OrderService orderService;
    private final UserService userService;
    private final EmailSenderService emailSenderService;
    @Autowired
    public CourierService(OrderService orderService, UserService userService,
                          EmailSenderService emailSenderService) {
        this.orderService = orderService;
        this.userService = userService;
        this.emailSenderService = emailSenderService;
    }


    public CourierAssignResponse assignOrder(Long id) {
        try {
            User courier = userService.getCurrentUser();

            if(isValidAssignOrder(courier.getId())){
                throw new IllegalStateException(
                        "Невозможно взять новый заказ у курьера уже есть активный заказ в доставке."
                );
            }

            Order updateOrder = orderService.setCourier( orderService.getById(id), courier.getId());
            emailSenderService.sendMessage(updateOrder.getUser().getEmail(),"Курьер взял ваш заказ ","Курьер ведет идет к вам," + courier.getName());
            log.info("Курьер с id: " + courier.getId() + " взял заказ с id:" + updateOrder.getId());


            return new CourierAssignResponse(//TODO Переделать возможно DTO
                    updateOrder.getId(),
                    courier.getId(),
                    courier.getName()
            );

        }catch (IllegalStateException e) {
            log.warn("Невозможно взять заказ: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Ошибка при взятии заказа курьером: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка при взятии заказа: " + e.getMessage(), e);
        }
    }

    public SetStatusOrderResponse setStatus(Long id, Order.OrderStatus status) {
        try {
            Order orderUpdate = orderService.setStatus(orderService.getById(id), status);
            if(status.equals(Order.OrderStatus.DELIVERED_TO_DESTINATION)) {
                emailSenderService.sendMessage(
                        orderUpdate.getUser().getEmail(),
                        "Курьер привез заказ",
                        "Курьер уже приехал к вам заберите заказ");
            }
            log.info("У заказа с id:" + id + " изменен статус на" + status.name());

            return new SetStatusOrderResponse(//TODO Переделать возможно DTO
                    orderUpdate.getId(),
                    status.name()
            );
        }
        catch (Exception e) {
            log.error("Ошибка при изменении статуса заказа: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка при изменении статуса заказа: " + e.getMessage(), e);
        }
    }


    private boolean isValidAssignOrder(Long courierId) {
        for (Order order : orderService.assignedCourierOrders(courierId)) {
            if(order.getStatus() == Order.OrderStatus.PENDING || order.getStatus() == Order.OrderStatus.DISPATCHED) {
                log.error( "Невозможно взять новый заказ: у курьера уже есть активный заказ в доставке.ID активного заказа={}", order.getId());
                return true;
            }
        }
        return false;
    }
}
