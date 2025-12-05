package org.example.kortex.users.db;

import org.example.kortex.carts.db.Cart;
import org.example.kortex.orders.db.Order;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true, nullable = false,length = 128)
    private String email;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "password", nullable = false, length = 64)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "address")
    private String address;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Column(name = "orders", nullable = false)
    private List<Order> orders = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cart cart;

    public User(Long id, String email, String name, String password, Role role, String address, List<Order> orders, Cart cart) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role;
        this.address = address;
        this.orders = orders;
        this.cart = cart;
    }

    public User(String email, String name, String password, String address, Cart cart) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = Role.USER;
        this.address = address;
        this.cart = cart;
    }

    public User() {
    }

    public void addOrderItem(Order order) {
        orders.add(order);
    }

    public void removeOrderItem(Order order) {
        orders.remove(order);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }


    public enum Role {
        USER, ADMIN, COURIER, SELLER;

        public SimpleGrantedAuthority toAuthority() {
            return new SimpleGrantedAuthority("ROLE_" + this.name());
        }
    }
}