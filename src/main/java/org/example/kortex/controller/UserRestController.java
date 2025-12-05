package org.example.kortex.controller;

import org.example.kortex.entity.Cart;
import org.example.kortex.entity.User;
import org.example.kortex.service.CartService;
import org.example.kortex.service.EmailSenderService;
import org.example.kortex.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;
    private final CartService cartService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserRestController(UserService userService, CartService cartService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.cartService = cartService;
        this.passwordEncoder = passwordEncoder;
    }


    @GetMapping("/{email}")
    public ResponseEntity<User> getUserEmail(@PathVariable String email) {
        return ResponseEntity.ok().body(userService.getByEmail(email));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUser() {
        return ResponseEntity.ok().body(userService.getAll());
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

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id,@RequestBody User user) {
        try {
            User existingUser = userService.getById(id);

            if (user.getName() != null) {
                existingUser.setName(user.getName());
            }
            if (user.getPassword() != null) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            if (user.getRole() != null) {
                existingUser.setRole(user.getRole());
            }

            User updatedUser = userService.update(id,existingUser);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {

        try {
            User user = userService.getById(id);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Пользователь не найден");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userService.update(id, user);

            return ResponseEntity.ok("Пароль изменен");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка изменения пароля: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/address")
    public ResponseEntity<?> changeAddress(
            @PathVariable Long id,
            @RequestParam String newAddress) {

        try {
            User user = userService.getById(id);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Пользователь не найден");
            }

            user.setAddress(newAddress);
            userService.update(id, user);

            return ResponseEntity.ok("Пароль изменен");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка изменения пароля: " + e.getMessage());
        }
    }
}
