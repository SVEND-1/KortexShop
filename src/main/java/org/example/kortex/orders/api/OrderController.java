package org.example.kortex.orders.api;

import org.example.kortex.cartItems.db.CartItem;
import org.example.kortex.cartItems.domain.CartItemService;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.domain.CartService;
import org.example.kortex.orders.domain.OrderService;
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
@RequestMapping("/api/orders")
public class OrderController {//todo
    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;
    private final CartItemService cartItemService;

    @Autowired
    public OrderController(OrderService orderService, UserService userService, CartService cartService, CartItemService cartItemService) {
        this.orderService = orderService;
        this.userService = userService;
        this.cartService = cartService;
        this.cartItemService = cartItemService;
    }

    @GetMapping("/me-create")
    public ResponseEntity<?> getMeCreateOrders() {
        try {
            User user = userService.getCurrentUser();
            Cart cart = cartService.getCartByUserId(userService.getCurrentUser().getId());
            List<CartItem> cartItems = cartItemService.findAllByCartId(cart.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("cartItems", cartItems);
            response.put("totalPrice", cart.totalPrice());
            response.put("totalItems", cart.getQuantity());
            response.put("user", user);
            response.put("redirect","/orders");

            return ResponseEntity.ok().body(response);
        }catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при получении данных заказов: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping()
    public ResponseEntity<?> createOrder() {
        try {
            User user = userService.getCurrentUser();
            Map<String, Object> response = new HashMap<>();
            response.put("order", orderService.createOrderFromCart(user.getId()));
            response.put("redirect","/");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при получении данных корзины: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
