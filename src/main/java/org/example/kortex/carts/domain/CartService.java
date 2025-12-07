package org.example.kortex.carts.domain;

import javax.persistence.EntityNotFoundException;
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

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemService cartItemService;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public CartService(CartRepository cartRepository, CartItemService cartItemService) {
        this.cartRepository = cartRepository;
        this.cartItemService = cartItemService;
    }


    public Cart getCartByUserId(Long userId) {//TODO Переписать
        Cart cart = cartRepository.findByUserId(userId);
        if (cart != null) {
            Hibernate.initialize(cart.getCartItems());
            for (CartItem item : cart.getCartItems()) {
                Hibernate.initialize(item.getProduct());
            }
        }
        return cart;
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
        Cart cart = cartRepository.findByUserId(userID);
        cart.clearCart();
        Cart saveCart = cartRepository.save(cart);
        log.info("Корзина очищена");
        return saveCart;
    }

    public Cart cartAddProduct(Long cartId, Long productId) {
        log.info("Добавление продукта " + productId + " в корзину " + cartId);
        Cart cart = getById(cartId);
        cartItemService.addItemToCart(cartId,productId,1);
        Cart saveCart = cartRepository.save(cart);
        log.info("Продукт добавлен успешно");
        return saveCart;
    }
}
