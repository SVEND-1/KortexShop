package org.example.kortex.users.api;

import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/couriers")
    public ResponseEntity<?> getCouriers() {
        try {
            return ResponseEntity.ok().body(userService.getAll());
        }
        catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при получении курьеров: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/sellers")
    public ResponseEntity<?> getSellers() {
        try {
            return ResponseEntity.ok().body(userService.getAll());
        }
        catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при получении продавцов: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
//TODO реализовать

//    @GetMapping
//    public ResponseEntity<?> getRequestForCouriers() {
//
//    }
//
//    @GetMapping
//    public ResponseEntity<?> getRequestForSellers() {
//
//    }

    @PostMapping("/appoint-courier")
    public ResponseEntity<?> appointCourier(@RequestParam Long userId) {
        try {
            User user = userService.appoint(userId, User.Role.COURIER);
            return ResponseEntity.ok().body(user);
        }
        catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка принятия курьера " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/appoint-seller")
    public ResponseEntity<?> appointSeller(@RequestParam Long userId) {
        try {
            User user = userService.appoint(userId, User.Role.SELLER);
            return ResponseEntity.ok().body(user);
        }
        catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка принятия продавца " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/downgrade-courier")
    public ResponseEntity<?>  downgradeCourier(@RequestParam Long userId) {
        try {
            User user = userService.downgrade(userId, User.Role.COURIER);
            return ResponseEntity.ok().body(user);
        }
        catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка снятия курьера " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/downgrade-seller")
    public ResponseEntity<?> downgradeSeller(@RequestParam Long userId) {
        try {
            User user = userService.downgrade(userId, User.Role.SELLER);
            return ResponseEntity.ok().body(user);
        }
        catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка снятия продавца " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
