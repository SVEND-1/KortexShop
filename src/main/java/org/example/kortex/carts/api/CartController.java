package org.example.kortex.carts.api;


import org.example.kortex.cartItems.db.CartItem;
import org.example.kortex.cartItems.domain.CartItemService;
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/items")
    public ResponseEntity<?> addItemToCart(@RequestParam Long productId) {
        try {
            User user = userService.getCurrentUser();
            Cart cart = cartService.getCartByUserId(user.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(cartService.cartAddProduct(cart.getId(), productId));
        }
        catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при добавлении товара в корзину: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


    @PatchMapping("/items/{itemId}/increase")
    public ResponseEntity<?> increaseQuantity(@PathVariable Long itemId) {
        try {
            CartItem item = cartItemService.findById(itemId);
            CartItem updated = cartItemService.updateQuantity(itemId, item.getQuantity() + 1);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/items/{itemId}/decrease")
    public ResponseEntity<?> decreaseQuantity(@PathVariable Long itemId) {
        try {
            CartItem item = cartItemService.findById(itemId);

            if (item.getQuantity() <= 1) {
                cartItemService.removeItemFromCart(itemId);
                return ResponseEntity.ok(Map.of("message", "Товар удален из корзины"));
            }

            CartItem updated = cartItemService.updateQuantity(itemId, item.getQuantity() - 1);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeItemFromCart(@PathVariable Long itemId) {
        try {
            cartItemService.removeItemFromCart(itemId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
