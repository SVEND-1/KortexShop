package org.example.kortex.users.api;

import io.swagger.v3.oas.annotations.Operation;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Получение профиля пользователя")
    @GetMapping("/me")
    public ResponseEntity<?> profile(){
        return ResponseEntity.ok().body(userService.getProfile());
    }

    @Operation(summary = "Получение истории заказов пользователя")
    @GetMapping("/me-orders")
    public ResponseEntity<?> orders(){
        return ResponseEntity.ok(userService.meOrders());
    }

    @Operation(summary = "Изменения адреса")
    @PostMapping("/address")
    public ResponseEntity<?> changeAddress(@RequestParam String newAddress) {
        return ResponseEntity.ok().body(userService.changeAddress(newAddress));
    }
}

