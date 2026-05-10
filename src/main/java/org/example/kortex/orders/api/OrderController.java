package org.example.kortex.orders.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kortex.orders.domain.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order",description = "Работа в заказами")
public class OrderController {
    private final OrderService orderService;
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @GetMapping
    public ResponseEntity<?> getOrders() {
        return ResponseEntity.ok(orderService.getHistoryOrders());
    }

    @Operation(summary = "Получить страницу заказа")
    @GetMapping("/me-create")
    public ResponseEntity<?> getMeCreateOrders() {
        return ResponseEntity.ok(orderService.getPageCreateOrder());
    }

    @Operation(summary = "Создать заказ")
    @PostMapping()
    public ResponseEntity<?> createOrder(@RequestParam String comment) {
        return ResponseEntity.ok(orderService.createOrderFromCart(comment));
    }
}

