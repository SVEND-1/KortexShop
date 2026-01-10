package org.example.kortex.users.api;

import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> profile(){
        return ResponseEntity.ok().body(userService.getProfile());
    }

    @GetMapping("/me-orders")
    public ResponseEntity<?> orders(){
        return ResponseEntity.ok(userService.meOrders());
    }

    @PostMapping("/address")
    public ResponseEntity<?> changeAddress(@RequestParam String newAddress) {
        return ResponseEntity.ok().body(userService.changeAddress(newAddress));
    }
}

