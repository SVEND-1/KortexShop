package org.example.kortex.carts.domain;

import javax.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.products.db.Product;
import org.example.kortex.carts.db.CartItemRepository;
import org.example.kortex.carts.db.CartRepository;
import org.example.kortex.products.db.ProductRepository;
import org.example.kortex.users.domain.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CartItemService(CartItemRepository cartItemRepository,
                           CartRepository cartRepository,
                           ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public CartItem findById(Long id) {
        return cartItemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("CartItem не найден"));
    }

    public List<CartItem> findAllByCartId(Long cartId) {
        return cartItemRepository.findAllByCartId(cartId);
    }

    public CartItem addItemToCart(Long cartId, Long productId, Integer quantity) {
        log.info("Добавление CartItem в корзину " + cartId);
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Корзина не найдена"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Продукт не найден"));

        if (quantity == null || quantity <= 0) {
            log.error("Количество должно быть больше 0");
            throw new IllegalArgumentException("Количество должно быть больше 0");
        }

        Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndProductId(cartId, productId);

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.calculatePrice();

            CartItem savedCartItem = cartItemRepository.save(cartItem);
            log.info("CartItem уже был и добавилось количество товара к нему id: " + savedCartItem.getId());
            return savedCartItem;
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.calculatePrice();

            CartItem savedCartItem = cartItemRepository.save(cartItem);
            log.info("Создан CartItem его id: " + savedCartItem.getId());
            return savedCartItem;
        }
    }

    public CartItem updateQuantity(Long cartItemId, Integer quantity) {
        log.info("Обновление количество товара в cartItem" + cartItemId + " на " + quantity);
        if (quantity == null || quantity <= 0) {
            log.error("Количество должно быть больше 0");
            throw new IllegalArgumentException("Количество должно быть больше 0");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem не найден"));

        cartItem.setQuantity(quantity);
        cartItem.calculatePrice();

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        log.info("Количество товара изменено");
        return savedCartItem;
    }

    public void removeItemFromCart(Long cartItemId) {
        log.info("Удаление товара из корзины cartItem id:" + cartItemId);
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new EntityNotFoundException("CartItem не найден");
        }

        cartItemRepository.deleteById(cartItemId);
        log.info("Товар удален из корзины");
    }

}
