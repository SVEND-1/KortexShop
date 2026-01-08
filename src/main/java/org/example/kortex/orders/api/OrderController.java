package org.example.kortex.orders.api;

import org.example.kortex.orders.domain.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/me-create")//Отображение страницы заказа
    public ResponseEntity<?> getMeCreateOrders() {
        return ResponseEntity.ok(orderService.getPageCreateOrder());
    }

    @PostMapping()
    public ResponseEntity<?> createOrder() {
        return ResponseEntity.ok(orderService.createOrderFromCart());
    }
}

