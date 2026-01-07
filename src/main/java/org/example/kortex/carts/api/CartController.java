package org.example.kortex.carts.api;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.api.dto.CartMapper;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.domain.CartItemService;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.domain.CartService;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;
    private final UserService userService;
    private final CartItemService cartItemService;
    private final CartMapper cartMapper;

    @Autowired
    public CartController(CartService cartService, UserService userService, CartItemService cartItemService,CartMapper cartMapper) {
        this.cartService = cartService;
        this.userService = userService;
        this.cartItemService = cartItemService;
        this.cartMapper = cartMapper;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCartPage() {
        return ResponseEntity.ok(cartService.getCartPage());
    }

    @PostMapping("/items")
    public ResponseEntity<?> addItemToCart(@RequestParam Long productId) {
        return ResponseEntity.ok(cartService.addItemToCart(productId));
    }

    @PatchMapping("/items/{itemId}/increase")
    public ResponseEntity<?> increaseQuantity(@PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.increaseQuantity(itemId));
    }

    @PatchMapping("/items/{itemId}/decrease")
    public ResponseEntity<?> decreaseQuantity(@PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.decreaseQuantity(itemId));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeItemFromCart(@PathVariable Long itemId) {
        cartService.removeItemFromCart(itemId);
        return ResponseEntity.ok().build();
    }
}
