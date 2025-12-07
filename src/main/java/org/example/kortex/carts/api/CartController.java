package org.example.kortex.carts.api;


import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;
    private final UserService userService;
    private final CartItemService cartItemService;

    @Autowired
    public CartController(CartService cartService, UserService userService, CartItemService cartItemService) {
        this.cartService = cartService;
        this.userService = userService;
        this.cartItemService = cartItemService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCartPage() {
        try {
            Cart cart = cartService.getCartByUserId(userService.getCurrentUser().getId());
            List<CartItem> cartItems = cartItemService.findAllByCartId(cart.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("cartItems", cartItems);
            response.put("totalPrice", cart.totalPrice());
            response.put("totalItems", cart.getQuantity());
            return ResponseEntity.ok().body(response);
        }catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при получении данных корзины: " + e.getMessage());
            log.error("Ошибка при получении данных корзины: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/items")
    public ResponseEntity<?> addItemToCart(@RequestParam Long productId) {
        try {
            log.info("Добавление товара в корзину");
            User user = userService.getCurrentUser();
            Cart cart = cartService.cartAddProduct(cartService.getCartByUserId(user.getId()).getId(), productId);
            log.info("Продукт с id: " + productId + " добавлен в корзину с id: " + cart.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(cart);
        }
        catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при добавлении товара в корзину: " + e.getMessage());
            log.error("Ошибка при добавлении товара в корзину: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    @PatchMapping("/items/{itemId}/increase")
    public ResponseEntity<?> increaseQuantity(@PathVariable Long itemId) {
        try {
            log.info("Инкремент товара");
            CartItem item = cartItemService.findById(itemId);
            CartItem updated = cartItemService.updateQuantity(itemId, item.getQuantity() + 1);
            log.info("Успешно инкремент товара id cartItem: " + updated.getId());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Ошибка инкремента: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/items/{itemId}/decrease")
    public ResponseEntity<?> decreaseQuantity(@PathVariable Long itemId) {
        try {
            log.info("Декрменет товара");
            CartItem item = cartItemService.findById(itemId);

            if (item.getQuantity() <= 1) {
                cartItemService.removeItemFromCart(itemId);
                log.info("Товар удален из корзины id cartItem: " + item.getId());
                return ResponseEntity.ok(Map.of("message", "Товар удален из корзины"));
            }

            CartItem updated = cartItemService.updateQuantity(itemId, item.getQuantity() - 1);
            log.info("Успешно декремент товара id cartItem: " + updated.getId());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Ошибка декремента: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeItemFromCart(@PathVariable Long itemId) {
        try {
            log.info("Удаление товара из корзины id cartItem: " + itemId);
            cartItemService.removeItemFromCart(itemId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Не удалось удалить товар: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
