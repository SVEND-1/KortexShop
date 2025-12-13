package org.example.kortex.products.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.example.kortex.carts.db.CartItem;
import org.example.kortex.orders.db.OrderItem;
import org.example.kortex.users.db.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "seller_id", nullable = false)
    @JsonIgnore
    private User seller;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "count", nullable = false)
    private int count;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "image", nullable = false)
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Category category;


    public Product(User seller,String name, Double price, int count, String description, String image, Category category) {
        this.seller = seller;
        this.name = name;
        this.price = price;
        this.count = count;
        this.description = description;
        this.image = image;
        this.category = category;
    }

    public Product() {

    }


    public enum Category {
        ELECTRONICS("Электроника"), CLOTHING("Одежда"), BOOKS("Книги"), FOOD("Еда"),
        SPORTS("Спорт товары"), HOME("Товары для дома"), BEAUTY("Красота"), OTHER("Другое");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public SimpleGrantedAuthority toAuthority() {
            return new SimpleGrantedAuthority("ROLE_" + this.name());
        }
    }
}
