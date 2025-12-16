package org.example.kortex.orders.api;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.api.dto.CartMapper;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.orders.api.dto.OrderMapper;
import org.example.kortex.orders.db.Order;
import org.example.kortex.orders.domain.OrderService;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.EmailSenderService;
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
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;
    private final CartMapper cartMapper;
    private final OrderMapper orderMapper;
    private final EmailSenderService emailSenderService;

    @Autowired
    public OrderController(OrderService orderService, UserService userService,CartMapper cartMapper,OrderMapper orderMapper,EmailSenderService emailSenderService) {
        this.orderService = orderService;
        this.userService = userService;
        this.cartMapper = cartMapper;
        this.orderMapper = orderMapper;
        this.emailSenderService = emailSenderService;
    }

    @GetMapping("/me-create")//Отображение страницы заказа
    public ResponseEntity<?> getMeCreateOrders() {
        try {
            User user = userService.getCurrentUserCart();
            Cart cart = user.getCart();
            List<CartItem> cartItems = cart.getCartItems();

            Map<String, Object> response = new HashMap<>();
            response.put("cartItems", cartMapper.toListCartItemDto(cartItems));
            response.put("totalPrice", cart.totalPrice());
            response.put("totalItems", cart.getQuantity());
            response.put("user", orderMapper.toUserOrderDto(user));

            return ResponseEntity.ok().body(response);
        }catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при получении данных заказов: " + e.getMessage());
            log.error("Ошибка при получении данных заказов: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping()
    public ResponseEntity<?> createOrder() {
        try {
            log.info("Создания заказа");
            User user = userService.getCurrentUser();
            Order order = orderService.createOrderFromCart(user.getId());

            emailSenderService.sendMessage(user.getEmail(),"Заказ успешно создан!"
                    ,"Мы отравим вам письмо когда курьер возьмет его");

            Map<String, Object> response = new HashMap<>();
            response.put("order", order);
            response.put("redirect","/");

            log.info("Заказ создан id: " + order.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при получении данных корзины: " + e.getMessage());
            log.error("Ошибка при получении данных корзины: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
