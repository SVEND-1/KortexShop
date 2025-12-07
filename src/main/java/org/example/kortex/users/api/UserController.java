package org.example.kortex.users.api;

import org.example.kortex.carts.db.Cart;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.domain.OrderService;
import org.example.kortex.users.db.User;
import org.example.kortex.carts.domain.CartService;
import org.example.kortex.users.domain.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final OrderService orderService;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserController(UserService userService,OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }


    @GetMapping("/me")
    public ResponseEntity<?> profile(){
        try {
            return ResponseEntity.ok().body(userService.getCurrentUser());
        }
        catch (Exception e) {
            log.error("Не удалось загрущить профиль" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/me-orders")
    public ResponseEntity<?> orders(){
        try {
            User user = userService.getCurrentUser();
            List<Order> userOrders = orderService.getOrdersByUserId(user.getId());
            return ResponseEntity.ok().body(userOrders);
        }
        catch (Exception e) {
            log.error("Не удалось загрузить заказы пользователя " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/address")
    public ResponseEntity<?> changeAddress(
            @RequestParam String newAddress) {

        try {
            User user = userService.getCurrentUser();
            log.info("Обновление адреса у пользователя с id: " + user.getId());

            user.setAddress(newAddress);
            userService.update(user.getId(), user);
            log.info("Адрес изменен");
            return ResponseEntity.ok("адрес изменен");
        } catch (Exception e) {
            log.error("Ошибка изменения адреса: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка изменения адреса: " + e.getMessage());
        }
    }
}
