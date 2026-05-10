package org.example.kortex.carts.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.example.kortex.users.db.User;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();


    public BigDecimal totalPrice() {
        if (cartItems == null || cartItems.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return cartItems.stream()
                .map(CartItem::calculatePrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getQuantity() {
        List<Integer> result = new ArrayList<>();
        cartItems.forEach(el -> {result.add(el.getQuantity());});
        return result.stream().reduce(0, Integer::sum);
    }


    public void clearCart() {
        this.cartItems.clear();
    }
}