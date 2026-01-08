package org.example.kortex.carts.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.api.dto.CartMapper;
import org.example.kortex.carts.api.dto.CartResponse;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.db.CartRepository;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    //================================Controller Methods================================================

    public CartResponse getCartPage() {
        try {
            User user = userService.getCurrentUserCart();
            Cart cart = user.getCart();
            return cartMapper.toCartResponse(cart);
        }catch (Exception e) {
            log.error("Ошибка при получении данных корзины, ex={}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка сервера при получении корзины"
            );
        }
    }

    @Transactional
    public CartResponse addItemToCart(Long productId) {
        try {
            log.info("Добавление товара в корзину");
            User user = userService.getCurrentUserCart();
            cartItemService.addItemToCart(user.getCart().getId(),productId);
            Cart saveCart = cartRepository.save(user.getCart());
            log.info("Продукт с id={} добавлен в корзину с id={}" ,productId, saveCart.getId());
            return cartMapper.toCartResponse(saveCart);
        }
        catch (Exception e) {
            log.error("Ошибка при добавлении товара в корзину, ex={}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка при добавлении товара в корзину"
            );
        }
    }

    public void increaseQuantity(Long itemId) {
        try {
            cartItemService.updateIncrement(itemId);
        }
        catch (Exception e) {
            log.error("Ошибка инкремента элемента корзины id={},ex={} ",itemId, e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка при увеличении количества"
            );
        }
    }

    public void decreaseQuantity(Long itemId) {
        try {
            CartItem updated = cartItemService.decreaseQuantityOrRemove(itemId);

            if (updated == null) {
                log.info("Элемент корзины id={} удален из корзины", itemId);
            }
        }
        catch (Exception e) {
            log.error("Ошибка декремента элемента корзины id={},ex={}",itemId, e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка при уменьшении количества"
            );
        }
    }

    public void removeItemFromCart( Long itemId) {
        try {
            log.info("Удаление товара из корзины cartItemId={}", itemId);
            cartItemService.removeItemFromCart(itemId);
        }
        catch (Exception e) {
            log.error("Не удалось удалить товар, ex={}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка при удалении товара из корзины"
            );
        }
    }

    //================================Service Methods================================================

    public Cart getCartWithUser(Long userId) {
        return cartRepository.findByUserIdWithItemsAndUser(userId);
    }

    public Cart create(Cart cartToCreate) {
        try {
            log.info("Создания корзины у пользователя id={}", cartToCreate.getUser().getId());
            Cart cart = cartRepository.save(cartToCreate);
            log.info("Корзина создана id={}", cart.getId());
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

    public void clearCartByUserId(Long userID)  {
        log.info("Очистка корзины");
        try {
            Cart cart = getCartWithUser(userID);
            cart.clearCart();
            cartRepository.save(cart);
            log.info("Корзина очищена");
        }catch (Exception e) {
            log.error("Не удалось очистить корзину пользователя id={},ex={}", userID, e.getMessage());
        }
    }

}
