package org.example.kortex.carts.db;

import lombok.Data;
import org.example.kortex.users.db.User;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Entity
@Table(name = "carts")
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


    public void clearCart() {
        this.cartItems.clear();
        this.totalPrice = BigDecimal.ZERO;
    }
}