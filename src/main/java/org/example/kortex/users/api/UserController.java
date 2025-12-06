package org.example.kortex.users.api;

import org.example.kortex.carts.db.Cart;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.domain.OrderService;
import org.example.kortex.users.db.User;
import org.example.kortex.carts.domain.CartService;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    //todo профиль
    private final UserService userService;
    private final CartService cartService;
    private final OrderService orderService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, CartService cartService,OrderService orderService ,PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.passwordEncoder = passwordEncoder;
    }


    @GetMapping("/me")
    public ResponseEntity<?> profile(){
        try {
            return ResponseEntity.ok().body(userService.getCurrentUser());
        }
        catch (Exception e) {
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<User> addUser(@RequestParam String email,
                                        @RequestParam String password,
                                        @RequestParam String name) {
        try {
            if (userService.getByEmail(email) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(User.Role.USER);

            User savedUser = userService.create(user);

            Cart cart = new Cart();
            cart.setUser(savedUser);
            cartService.create(cart);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/password")
    public ResponseEntity<?> changePassword(
            @RequestParam String newPassword) {

        try {
            User user = userService.getCurrentUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.update(user.getId(), user);

            return ResponseEntity.ok("Пароль изменен");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка изменения пароля: " + e.getMessage());
        }
    }

    @PostMapping("/address")
    public ResponseEntity<?> changeAddress(
            @RequestParam String newAddress) {

        try {
            User user = userService.getCurrentUser();

            user.setAddress(newAddress);
            userService.update(user.getId(), user);

            return ResponseEntity.ok("Пароль изменен");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка изменения пароля: " + e.getMessage());
        }
    }
}
