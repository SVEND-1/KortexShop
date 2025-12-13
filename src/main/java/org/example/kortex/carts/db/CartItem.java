package org.example.kortex.carts.db;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.example.kortex.products.db.Product;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
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

    public CartItem(Long id, Cart cart, Product product, Integer quantity, BigDecimal price) {
        this.id = id;
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        calculatePrice();
    }

    public CartItem() {
    }


    public void calculatePrice() {
        if (product != null && product.getPrice() != null) {
            this.price = BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(quantity));
        }
    }
}