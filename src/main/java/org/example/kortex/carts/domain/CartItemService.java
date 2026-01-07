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

    @Transactional
    public CartItem addItemToCart(Long cartId, Long productId) {
        log.info("Добавление CartItem в корзину " + cartId);
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Корзина не найдена"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Продукт не найден"));


        Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndProductId(cartId, productId);

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            cartItem.calculatePrice();

            CartItem savedCartItem = cartItemRepository.save(cartItem);
            log.info("CartItem уже был и добавилось количество товара к нему id: " + savedCartItem.getId());
            return savedCartItem;
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(1);
            cartItem.calculatePrice();

            CartItem savedCartItem = cartItemRepository.save(cartItem);
            log.info("Создан CartItem его id: " + savedCartItem.getId());
            return savedCartItem;
        }
    }

    @Transactional
    public CartItem updateIncrement(Long cartItemId) {
        log.info("Увеличение количества товара в cartItem: {}", cartItemId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem не найден"));

        Product product = cartItem.getProduct();
        if (product.getCount() <= 0) {
            throw new IllegalStateException("Товар отсутствует на складе");
        }
        if(product.getCount() > cartItem.getQuantity()) {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            cartItem.calculatePrice();

            CartItem savedCartItem = cartItemRepository.save(cartItem);
            log.info("Количество товара увеличено до: {}", savedCartItem.getQuantity());
            return savedCartItem;
        }
        else {
            log.info("Достингут лимит товаров на складе");
            return cartItem;
        }
    }


    @Transactional
    public CartItem decreaseQuantityOrRemove(Long cartItemId) {
        log.info("Уменьшение количества или удаление cartItem: {}", cartItemId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem не найден"));

        if (cartItem.getQuantity() <= 1) {
            removeItemFromCart(cartItemId);
            return null;
        }

        cartItem.setQuantity(cartItem.getQuantity() - 1);
        cartItem.calculatePrice();

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        log.info("Количество товара уменьшено до: {}", savedCartItem.getQuantity());
        return savedCartItem;
    }

    @Transactional
    public void removeItemFromCart(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem не найден"));

        // Возвращаем все количество товара на склад
        Product product = cartItem.getProduct();
        product.setCount(product.getCount() + cartItem.getQuantity());

        cartItemRepository.delete(cartItem);
        log.info("CartItem удален: {}", cartItemId);
    }


}
