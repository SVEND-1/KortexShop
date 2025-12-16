package org.example.kortex.users.api;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.orders.api.OrdersSearchCourierFilter;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.domain.OrderService;
import org.example.kortex.users.api.dto.courier.CourierDTOMapper;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.EmailSenderService;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    private final EmailSenderService emailSenderService;
    private final CourierDTOMapper courierDTOMapper;

    @Autowired
    public CourierController(OrderService orderService, UserService userService,EmailSenderService emailSenderService,CourierDTOMapper courierDTOMapper) {
        this.orderService = orderService;
        this.userService = userService;
        this.emailSenderService = emailSenderService;
        this.courierDTOMapper = courierDTOMapper;
    }

    @GetMapping("/assignedOrders")
    public ResponseEntity<?> getAssignedOrders(
            @RequestParam(name = "courierId") Long courierId,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber
    ) {
        try {
            OrdersSearchCourierFilter filter = new OrdersSearchCourierFilter(courierId, pageSize, pageNumber);
            Page<Order> ordersPage = orderService.assignedCourierOrdersPage(filter);

            Map<String, Object> response = courierDTOMapper.toPageResponse(ordersPage);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при получении назначенных заказов: " + e.getMessage());
            log.error("Ошибка при получении назначенных заказов: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/availableOrders")//Доступные заказы
    public ResponseEntity<?> getAvailableOrders(@RequestParam(name = "pageSize",required = false) Integer pageSize,
                                                @RequestParam(name = "pageNumber", required = false) Integer pageNumber) {
        try {
            return ResponseEntity.ok().body(courierDTOMapper.toPageResponse(orderService.availableCourierOrdersPage(pageSize, pageNumber)));
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
                if(order.getStatus() == Order.OrderStatus.PENDING || order.getStatus() == Order.OrderStatus.DISPATCHED) {
                    log.error( "Невозможно взять новый заказ: у курьера уже есть активный заказ в доставке. " +
                            "ID активного заказа: " + order.getId());
                    throw new IllegalStateException(
                            "Невозможно взять новый заказ: у курьера уже есть активный заказ в доставке. " +
                                    "ID активного заказа: " + order.getId()
                    );
                }
            }

            Order updateOrder = orderService.setCourier( orderService.getById(id), courier.getId());
            emailSenderService.sendMessage(updateOrder.getUser().getEmail(),"Курьер взял ваш заказ ","Курьер ведет идет к вам," + courier.getName());
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
            if(status.equals(Order.OrderStatus.DELIVERED_TO_DESTINATION)) {
               emailSenderService.sendMessage(orderUpdate.getUser().getEmail(),"Курьер привез заказ","Курьер уже приехал к вам заберите заказ");
            }
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
