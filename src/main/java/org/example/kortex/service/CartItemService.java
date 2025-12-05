package org.example.kortex.service;

import javax.persistence.EntityNotFoundException;
import org.example.kortex.entity.Cart;
import org.example.kortex.entity.CartItem;
import org.example.kortex.entity.Product;
import org.example.kortex.entity.User;
import org.example.kortex.repository.CartItemRepository;
import org.example.kortex.repository.CartRepository;
import org.example.kortex.repository.ProductRepository;
import org.example.kortex.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


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
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Корзина не найдена"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Продукт не найден"));

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше 0");
        }

        Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndProductId(cartId, productId);

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.calculatePrice();
            return cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.calculatePrice();

            return cartItemRepository.save(cartItem);
        }
    }

    public CartItem updateQuantity(Long cartItemId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше 0");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem не найден"));

        cartItem.setQuantity(quantity);
        cartItem.calculatePrice();

        return cartItemRepository.save(cartItem);
    }

    public void removeItemFromCart(Long cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new EntityNotFoundException("CartItem не найден");
        }

        cartItemRepository.deleteById(cartItemId);
    }

    public void clearCart(Long cartId) {
        if (!cartRepository.existsById(cartId)) {
            throw new EntityNotFoundException("Корзина не найдена");
        }

        List<CartItem> cartItems = cartItemRepository.findAllByCartId(cartId);
        cartItemRepository.deleteAll(cartItems);
    }
}
