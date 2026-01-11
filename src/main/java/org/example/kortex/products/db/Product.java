package org.example.kortex.products.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.example.kortex.users.db.User;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
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
    private BigDecimal price;

    @Column(name = "count", nullable = false)
    private int count;

    @Column(name = "description", nullable = false,length = 3000)
    private String description;

    @Column(name = "image", nullable = false)
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Category category;


    public Product(User seller,String name, BigDecimal price, int count, String description, String image, Category category) {
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

}
