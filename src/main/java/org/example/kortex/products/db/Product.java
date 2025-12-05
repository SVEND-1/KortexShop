package org.example.kortex.products.db;

import org.example.kortex.cartItems.db.CartItem;
import org.example.kortex.orderItems.db.OrderItem;
import org.example.kortex.users.db.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "seller_id", nullable = false)
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

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItem> cartItems = new ArrayList<>();

    public Product(Long id,User seller, String name, Double price, int count, String description, String image, Category category, List<OrderItem> orderItems, List<CartItem> cartItems) {
        this.id = id;
        this.seller = seller;
        this.name = name;
        this.price = price;
        this.count = count;
        this.description = description;
        this.image = image;
        this.category = category;
        this.orderItems = orderItems;
        this.cartItems = cartItems;
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
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
