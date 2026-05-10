package org.example.kortex.carts.db;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.example.kortex.products.db.Product;

import javax.persistence.*;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @PrePersist
    @PreUpdate
    private void init(){
        calculatePrice();
    }

    public BigDecimal  calculatePrice() {
        if (product != null && product.getPrice() != null) {
            this.price = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
        return price;
    }

}