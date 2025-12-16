package org.example.kortex.users.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "role_requests")
public class RoleRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_role",nullable = false)
    private User.Role requestedRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_action",nullable = false)
    private TypeAction typeAction;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name="status",nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "create_at",nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }

    public enum TypeAction{
        REMOVE,
        ENHANCE
    }


    public RoleRequest() {}

    public RoleRequest(User user, User.Role requestedRole, String message) {
        this.user = user;
        this.requestedRole = requestedRole;
        this.message = message;
    }
}
