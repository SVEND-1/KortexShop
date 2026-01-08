package org.example.kortex.carts.api;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.domain.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
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
        cartService.increaseQuantity(itemId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/items/{itemId}/decrease")
    public ResponseEntity<?> decreaseQuantity(@PathVariable Long itemId) {
        cartService.decreaseQuantity(itemId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeItemFromCart(@PathVariable Long itemId) {
        cartService.removeItemFromCart(itemId);
        return ResponseEntity.ok().build();
    }
}
