package org.example.kortex.carts.domain;

import javax.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.carts.db.CartRepository;
import org.example.kortex.users.domain.UserService;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemService cartItemService;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemService cartItemService) {
        this.cartRepository = cartRepository;
        this.cartItemService = cartItemService;
    }


    public Cart getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId);
        return cart;
    }

    public Cart getCartWithUser(Long userId) {
        return cartRepository.findByUserIdWithItemsAndUser(userId);
    }

    public Cart getById(Long id)  {
        return cartRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("не найден"));
    }

    public Cart create(Cart cartToCreate) {
        log.info("Создания корзины у пользователя " + cartToCreate.getUser().getId());
        Cart cart = cartRepository.save(cartToCreate);
        log.info("Корзина создана id корзины: " + cart.getId());
        return cart;
    }

    public Cart clearCartByUserId(Long userID)  {
        log.info("Очистка корзины");
        Cart cart = getCartWithUser(userID);
        cart.clearCart();
        Cart saveCart = cartRepository.save(cart);
        log.info("Корзина очищена");
        return saveCart;
    }


    public Cart cartAddProduct(Cart cart, Long productId) {
        log.info("Добавление продукта " + productId + " в корзину " + cart.getId());
        cartItemService.addItemToCart(cart.getId(),productId,1);
        Cart saveCart = cartRepository.save(cart);
        log.info("Продукт добавлен успешно");
        return saveCart;
    }
}
