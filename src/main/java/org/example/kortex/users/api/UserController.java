package org.example.kortex.users.api;

import org.example.kortex.users.api.dto.user.UserMapper;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }


    @GetMapping("/me")
    public ResponseEntity<?> profile(){
        return ResponseEntity.ok().body(userMapper.toDto(userService.getCurrentUser()));
    }

    @GetMapping("/me-orders")
    public ResponseEntity<?> orders(){
        return ResponseEntity.ok(userService.meOrders());
    }

    @PostMapping("/address")
    public ResponseEntity<?> changeAddress(
            @RequestParam String newAddress) {
        return ResponseEntity.ok().body(userService.changeAddress(newAddress));
    }
}

