package org.example.kortex.carts.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.domain.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@Tag(name = "Cart",description = "Взаимодействеие с корзиной")
public class CartController {
    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "Получение корзины пользователя")
    @GetMapping("/me")
    public ResponseEntity<?> getCartPage() {
        return ResponseEntity.ok(cartService.getCartPage());
    }

    @Operation(summary = "Добавление товара в корзину")
    @PostMapping("/items")
    public ResponseEntity<?> addItemToCart(@RequestParam Long productId) {//Поменять на путь
        return ResponseEntity.ok(cartService.addItemToCart(productId));
    }

    @Operation(summary = "Добавление количесвто к товару")
    @PatchMapping("/items/{itemId}/increase")
    public ResponseEntity<?> increaseQuantity(@PathVariable Long itemId) {
        cartService.increaseQuantity(itemId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Уменьшение количество к товару")
    @PatchMapping("/items/{itemId}/decrease")
    public ResponseEntity<?> decreaseQuantity(@PathVariable Long itemId) {
        cartService.decreaseQuantity(itemId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удаление товара из корзины")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeItemFromCart(@PathVariable Long itemId) {
        cartService.removeItemFromCart(itemId);
        return ResponseEntity.ok().build();
    }
}
