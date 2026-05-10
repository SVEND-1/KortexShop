package org.example.kortex.carts.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.domain.mapper.CartMapper;
import org.example.kortex.carts.api.dto.CartResponse;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.db.CartRepository;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            throw new RuntimeException("Ошибка сервера при получении корзины",e);
        }
    }

    @Transactional
    public CartResponse addItemToCart(Long productId) {
        try {
            User user = userService.getCurrentUserCart();
            cartItemService.addItemToCart(user.getCart().getId(),productId);
            Cart saveCart = cartRepository.save(user.getCart());
            return cartMapper.toCartResponse(saveCart);
        }
        catch (Exception e) {
            log.error("Ошибка при добавлении товара в корзину, ex={}", e.getMessage());
            throw new RuntimeException("Ошибка при добавлении товара в корзину",e);
        }
    }

    public void increaseQuantity(Long itemId) {
        try {
            cartItemService.updateIncrement(itemId);
        }
        catch (Exception e) {
            log.error("Ошибка инкремента элемента корзины id={},ex={} ",itemId, e.getMessage());
            throw new RuntimeException("Ошибка при увеличении количества",e);
        }
    }

    public void decreaseQuantity(Long itemId) {
        try {
            cartItemService.decreaseQuantityOrRemove(itemId);
        }
        catch (Exception e) {
            throw new RuntimeException("Ошибка при уменьшении количества",e);
        }
    }

    public void removeItemFromCart( Long itemId) {
        try {
            cartItemService.removeItemFromCart(itemId);
        }
        catch (Exception e) {
            log.error("Не удалось удалить товар, ex={}", e.getMessage());
            throw new RuntimeException("Ошибка при удалении товара из корзины",e);
        }
    }

    //================================Service Methods================================================

    public Cart getCartWithUser(Long userId) {
        return cartRepository.findByUserIdWithItemsAndUser(userId);
    }

    public Cart create(Cart cartToCreate) {
        try {
            return cartRepository.save(cartToCreate);
        }
        catch (Exception e){
            log.error("Не удалось создать корзину, ex={}", e.getMessage());
            throw new RuntimeException("Внутренняя ошибка сервера при создании корзины",e);
        }
    }

    @Transactional
    public void clearCartByUserId(Long userID)  {
        try {
            Cart cart = getCartWithUser(userID);
            cart.clearCart();
            cartRepository.save(cart);
        }catch (Exception e) {
            log.error("Не удалось очистить корзину пользователя id={},ex={}", userID, e.getMessage());
            throw new RuntimeException("Не удалось очистить корзину ex=" + e);
        }
    }

}
