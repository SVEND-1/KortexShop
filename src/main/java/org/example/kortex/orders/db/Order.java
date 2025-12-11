package org.example.kortex.orders.db;

import lombok.Data;
import org.example.kortex.users.db.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "courier_id")
    private User courier;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "message",length = 500)
    private String message;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order() {
        this.orderDate = LocalDateTime.now();
    }

    public Order(Long id, User user,User courier, OrderStatus status,String message ,BigDecimal totalAmount, List<OrderItem> orderItems) {
        this.id = id;
        this.user = user;
        this.courier = courier;
        this.status = status;
        this.message = message;
        this.orderDate = LocalDateTime.now();
        this.shippingAddress = user.getAddress();
        this.totalAmount = totalAmount;
        this.orderItems = orderItems;
    }

    public enum OrderStatus {
        PENDING, DISPATCHED, DELIVERED_TO_DESTINATION, CANCELLED, RETURNED,COMPLETED;
        public SimpleGrantedAuthority toAuthority() {
            return new SimpleGrantedAuthority("ROLE_" + this.name());
        }
    }
}

