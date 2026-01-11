package org.example.kortex.carts.domain;

import javax.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.products.db.Product;
import org.example.kortex.carts.db.CartItemRepository;
import org.example.kortex.carts.db.CartRepository;
import org.example.kortex.products.db.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    //================================Controller Methods================================================

    //================================Service Methods================================================

    @Transactional
    public void addItemToCart(Long cartId, Long productId) {
        try {
            log.info("Добавление CartItem в корзину cartId={}", cartId);
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
                log.info("CartItem уже был в корзине и добавилось количество товара к нему cartItemId={}", savedCartItem.getId());
            } else {
                CartItem cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setProduct(product);
                cartItem.setQuantity(1);
                cartItem.calculatePrice();

                CartItem savedCartItem = cartItemRepository.save(cartItem);
                log.info("Создан CartItem id={}", savedCartItem.getId());
            }
        }catch (Exception e) {
            log.error("Не удалось создать элемент корзины, ex={}", e.getMessage());
        }
    }

    @Transactional
    public void updateIncrement(Long cartItemId) {
        try {
            log.info("Увеличение количества cartItem: {}", cartItemId);

            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new EntityNotFoundException("CartItem не найден"));

            Product product = cartItem.getProduct();
            if (product.getCount() <= 0) {
                log.warn("Товар отсутствует на складе");
                throw new IllegalStateException("Товар отсутствует на складе");
            }
            if (product.getCount() > cartItem.getQuantity()) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                cartItem.calculatePrice();

                cartItemRepository.save(cartItem);
            } else {
                log.warn("Достингут лимит товаров на складе");
            }
        }catch (Exception e){
            log.error("Не удалось увеличить количество элемент корзины, ex={}", e.getMessage());
        }
    }


    @Transactional
    public CartItem decreaseQuantityOrRemove(Long cartItemId) {
        try {
            log.info("Уменьшение количества cartItem: {}", cartItemId);

            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new EntityNotFoundException("CartItem не найден"));

            if (cartItem.getQuantity() <= 1) {
                removeItemFromCart(cartItemId);
                return null;
            }

            cartItem.setQuantity(cartItem.getQuantity() - 1);
            cartItem.calculatePrice();

            return cartItemRepository.save(cartItem);
        }catch (Exception e){
            log.error("Не удалось уменьшить количество элемента корзины, ex={}", e.getMessage());
            return null;
        }
    }

    @Transactional
    public void removeItemFromCart(Long cartItemId) {
        try {
            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new EntityNotFoundException("CartItem не найден"));

            cartItemRepository.delete(cartItem);
            log.info("CartItem удален id={}", cartItemId);
        }catch (Exception e){
            log.error("Не удалось удалить элемент корзины, ex={}", e.getMessage());
        }
    }


}
