package org.example.kortex.users.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.orders.db.Order;
import org.example.kortex.roleRequest.db.RoleRequest;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
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

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "address")
    private String address;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Column(name = "orders", nullable = false)
    @JsonIgnore
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<RoleRequest> roleRequests = new ArrayList<>();

    public User(Long id, String email, String name, String password, Role role, String address, List<Order> orders, Cart cart, List<RoleRequest> roleRequests) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role;
        this.address = address;
        this.orders = orders;
        this.cart = cart;
        this.roleRequests = roleRequests;
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

}