package org.example.kortex.carts.db;

import org.example.kortex.cartItems.db.CartItem;
import org.example.kortex.products.db.Product;
import org.example.kortex.users.db.User;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "cards")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @Column(name = "total_price")
    private BigDecimal totalPrice = BigDecimal.ZERO;

    public Cart() {
    }

    public Cart(Long id, User user, List<CartItem> cartItems, BigDecimal totalPrice) {
        this.id = id;
        this.user = user;
        this.cartItems = cartItems;
        this.totalPrice = totalPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void calculateTotalPrice() {
        this.totalPrice = cartItems.stream()
                .map(item -> {
                    item.calculatePrice();
                    return item.getPrice();
                })
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalPrice(){
        List<BigDecimal> result = new ArrayList<>();
        cartItems.forEach(el -> {result.add(el.getPrice());});
        return result.stream().reduce(BigDecimal.valueOf(0),BigDecimal::add);
    }

    public int getQuantity() {
        List<Integer> result = new ArrayList<>();
        cartItems.forEach(el -> {result.add(el.getQuantity());});
        return result.stream().reduce(0, Integer::sum);
    }

    public void addCartItem(Product product, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(this);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.calculatePrice();
        this.cartItems.add(cartItem);
        calculateTotalPrice();
    }

    public void removeCartItemByProduct(Long productId) {
        boolean removed = cartItems.removeIf(item ->
                item.getProduct() != null && item.getProduct().getId().equals(productId));
        if (removed) {
            calculateTotalPrice();
        }
    }

    public void clearCart() {
        this.cartItems.clear();
        this.totalPrice = BigDecimal.ZERO;
    }
}