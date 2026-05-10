package org.example.kortex.users.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kortex.orders.api.dto.OrdersSearchCourierFilter;
import org.example.kortex.orders.api.dto.OrderPageResponse;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.domain.OrderService;
import org.example.kortex.users.domain.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/couriers")
@Tag(name = "Courier",description = "Работа с курьером")
public class CourierController {
    private final OrderService orderService;
    private final CourierService courierService;

    @Autowired
    public CourierController(OrderService orderService,
                             CourierService courierService) {
        this.orderService = orderService;
        this.courierService = courierService;
    }

    @Operation(summary = "Получение принятых заказов")
    @GetMapping("/assignedOrders")
    public ResponseEntity<OrderPageResponse> getAssignedOrders(
            @ModelAttribute OrdersSearchCourierFilter filter) {

        return ResponseEntity.ok(orderService.assignedCourierOrdersPage(filter));
    }

    @Operation(summary = "Получение доступных заказов")
    @GetMapping("/availableOrders")
    public CompletableFuture<ResponseEntity<OrderPageResponse>> getAvailableOrders(
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber) {

        return orderService.availableCourierOrdersPage(pageSize, pageNumber)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Взять заказ")
    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assignOrder(@PathVariable Long id) {
        return ResponseEntity.ok(courierService.assignOrder(id));
    }

    @Operation(summary = "Сменить статус заказа")
    @PostMapping("/orders/{id}/status")
    public ResponseEntity<?> setStatus(@PathVariable Long id, @RequestParam("status") Order.OrderStatus status) {
       return ResponseEntity.ok(courierService.setStatus(id, status));
    }


}

