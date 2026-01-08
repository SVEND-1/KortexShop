package org.example.kortex.users.api;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.orders.api.OrdersSearchCourierFilter;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.domain.OrderService;
import org.example.kortex.users.api.dto.courier.CourierDTOMapper;
import org.example.kortex.users.domain.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/couriers")
public class CourierController {
    private final OrderService orderService;
    private final CourierService courierService;
    private final CourierDTOMapper courierDTOMapper;

    @Autowired
    public CourierController(OrderService orderService,CourierDTOMapper courierDTOMapper,
                             CourierService courierService) {
        this.orderService = orderService;
        this.courierDTOMapper = courierDTOMapper;
        this.courierService = courierService;
    }

    @GetMapping("/assignedOrders")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getAssignedOrders(
            @RequestParam(name = "courierId") Long courierId,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber
    ) {
        OrdersSearchCourierFilter filter = new OrdersSearchCourierFilter(courierId, pageSize, pageNumber);

        return orderService.assignedCourierOrdersPage(filter)
                .thenApply(ordersPage ->{
                   Map<String,Object> response = courierDTOMapper.toPageResponse(ordersPage);
                   return ResponseEntity.ok(response);
                })
                .exceptionally(ex -> {
                    Map<String,Object> error = new HashMap<>();
                    error.put("error","Не удалось загрузить данные заказов");
                    error.put("message",ex.getMessage());
                    log.error("Ошибка загрузке взятых заказов: {}" ,ex.getMessage());
                    return ResponseEntity.badRequest().body(error);
                });
    }

    @GetMapping("/availableOrders")//Доступные заказы
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getAvailableOrders(@RequestParam(name = "pageSize",required = false) Integer pageSize,
                                                @RequestParam(name = "pageNumber", required = false) Integer pageNumber) {
        return orderService.availableCourierOrdersPage(pageSize,pageNumber)
                .thenApply(orders -> {
                    Map<String,Object> response = courierDTOMapper.toPageResponse(orders);
                    return ResponseEntity.ok(response);
                })
                .exceptionally(ex ->{
                    Map<String,Object> error = new HashMap<>();
                    error.put("error","Не удалось загрузить доступные заказы");
                    error.put("message",ex.getMessage());
                    log.error("Ошибка загрузки доступных заказов: {}",ex.getMessage());
                    return ResponseEntity.badRequest().body(error);
                });
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assignOrder(@PathVariable Long id) {
        return ResponseEntity.ok(courierService.assignOrder(id));
    }

    @PostMapping("/orders/{id}/status")
    public ResponseEntity<?> setStatus(@PathVariable Long id, @RequestParam("status") Order.OrderStatus status) {
       return ResponseEntity.ok(courierService.setStatus(id, status));
    }


}

