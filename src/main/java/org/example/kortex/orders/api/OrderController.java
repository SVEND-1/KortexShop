package org.example.kortex.orders.api;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.api.dto.CartMapper;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.orders.api.dto.OrderMapper;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.domain.OrderService;
import org.example.kortex.users.api.dto.user.UserDTO;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
        return ResponseEntity.ok(orderService.getMeCreateOrders());
    }

    @PostMapping()
    public ResponseEntity<?> createOrder() {
        return ResponseEntity.ok(orderService.createOrder());
    }
}

