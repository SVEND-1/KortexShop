package org.example.kortex.users.api;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.orders.api.OrdersSearchCourierFilter;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.domain.OrderService;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/couriers")
public class CourierController {
    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public CourierController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/assignedOrders")//Заказы курьера
    public ResponseEntity<?> getAssignedOrders(
            @RequestParam(name = "courierId") Long courierId,
            @RequestParam(name = "pageSize",required = false) Integer pageSize,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber
    ) {
        try {
            OrdersSearchCourierFilter filter = new OrdersSearchCourierFilter(courierId, pageSize, pageNumber);
            return ResponseEntity.ok().body(orderService.assignedCourierOrdersPage(filter));
        }catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при получении выполненых заказов: " + e.getMessage());
            log.error("Ошибка при получении выполненых заказов: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/availableOrders")//Доступные заказы
    public ResponseEntity<?> getAvailableOrders(@RequestParam(name = "pageSize",required = false) Integer pageSize,
                                                 @RequestParam(name = "pageNumber", required = false) Integer pageNumber) {
        try {
            return ResponseEntity.ok().body(orderService.availableCourierOrdersPage(pageSize, pageNumber));
        }catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при получении доступных заказов: " + e.getMessage());
            log.error("Ошибка при получении доступных заказов: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assignOrder(@PathVariable Long id) {
        try {
            User courier = userService.getCurrentUser();

            for (Order order : orderService.assignedCourierOrders(courier.getId())) {
                if(order.getStatus() == Order.OrderStatus.DISPATCHED) {
                    log.error( "Невозможно взять новый заказ: у курьера уже есть активный заказ в доставке. " +
                            "ID активного заказа: " + order.getId());
                    throw new IllegalStateException(
                            "Невозможно взять новый заказ: у курьера уже есть активный заказ в доставке. " +
                                    "ID активного заказа: " + order.getId()
                    );
                }
            }

            Order updateOrder = orderService.setCourier( orderService.getById(id), courier.getId());

            log.info("Курьер с id: " + courier.getId() + " взял заказ с id:" + updateOrder.getId());

            return ResponseEntity.ok().body(updateOrder);
        }catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при взятия заказа курьером: " + e.getMessage());
            log.error("Ошибка при взятия заказа курьером: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/orders/{id}/status")
    public ResponseEntity<?> setStatus(@PathVariable Long id, @RequestParam("status") Order.OrderStatus status) {
        try {
            Order orderUpdate = orderService.setStatus(orderService.getById(id), status);

            log.info("У заказа с id:" + id + " изменен статус на" + status.name());
            return ResponseEntity.ok().body(orderUpdate);
        }
        catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при изменения статуса заказа: " + e.getMessage());
            log.error("Ошибка при изменения статуса заказа: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


}
