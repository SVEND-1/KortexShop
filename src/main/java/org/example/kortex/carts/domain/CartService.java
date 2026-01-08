package org.example.kortex.carts.domain;

import javax.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.api.dto.CartMapper;
import org.example.kortex.carts.api.dto.CartResponse;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.db.CartRepository;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemService cartItemService;
    private final UserService userService;
    private final CartMapper cartMapper;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemService cartItemService,
                       UserService userService,CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.cartItemService = cartItemService;
        this.userService = userService;
        this.cartMapper = cartMapper;
    }

    public Cart getCartWithUser(Long userId) {
        return cartRepository.findByUserIdWithItemsAndUser(userId);
    }

    public CartResponse getCartPage() {
        try {
            User user = userService.getCurrentUserCart();
            Cart cart = user.getCart();
            return cartMapper.toCartResponse(cart);
        }catch (Exception e) {
            log.error("Ошибка при получении данных корзины: " + e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка сервера при получении корзины"
            );
        }
    }

    public Cart create(Cart cartToCreate) {
        try {
            log.info("Создания корзины у пользователя " + cartToCreate.getUser().getId());
            Cart cart = cartRepository.save(cartToCreate);
            log.info("Корзина создана id корзины: " + cart.getId());
            return cart;
        }
        catch (Exception e){
            log.error("Не удалось создать корзину, ex={}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Внутренняя ошибка сервера при создании корзины"
            );
        }
    }

    @Transactional
    public Cart addItemToCart(Long productId) {
        try {
            log.info("Добавление товара в корзину");
            User user = userService.getCurrentUserCart();
            cartItemService.addItemToCart(user.getCart().getId(),productId);
            Cart saveCart = cartRepository.save(user.getCart());
            log.info("Продукт с id: " + productId + " добавлен в корзину с id: " + saveCart.getId());
            return saveCart;
        }
        catch (Exception e) {
            log.error("Ошибка при добавлении товара в корзину: " + e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка при добавлении товара в корзину"
            );
        }
    }

    public String increaseQuantity(Long itemId) {
        try {
            log.info("Инкремент товара");
            CartItem updated = cartItemService.updateIncrement(itemId);
            log.info("Успешно инкремент товара id cartItem: " + updated.getId());
            return "Успешно";
        }
        catch (Exception e) {
            log.error("Ошибка инкремента: " + e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка при увеличении количества"
            );
        }
    }

    public String decreaseQuantity( Long itemId) {
        try {
            log.info("Декрменет товара");

            CartItem updated = cartItemService.decreaseQuantityOrRemove(itemId);

            if (updated == null) {
                return "Товар удален из корзины";
            }

            log.info("Успешно декремент товара id cartItem: " + updated.getId());
            return "Успешно";
        }
        catch (Exception e) {
            log.error("Ошибка декремента: " + e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка при уменьшении количества"
            );
        }
    }

    public void removeItemFromCart( Long itemId) {
        try {
            log.info("Удаление товара из корзины id cartItem: " + itemId);
            cartItemService.removeItemFromCart(itemId);
        }
        catch (Exception e) {
            log.error("Не удалось удалить товар: " + e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка при удалении товара из корзины"
            );
        }
    }

    public void clearCartByUserId(Long userID)  {
        log.info("Очистка корзины");
        Cart cart = getCartWithUser(userID);
        cart.clearCart();
        Cart saveCart = cartRepository.save(cart);
        log.info("Корзина очищена");
    }

}
